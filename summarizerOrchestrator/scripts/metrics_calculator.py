import json
import sys
from nltk.translate.bleu_score import sentence_bleu, SmoothingFunction
from nltk.translate.meteor_score import meteor_score
from rouge_score import rouge_scorer
from bert_score import score
import os

# Disable flash attention warnings
os.environ["TORCH_CUDA_SDP_DISABLE_FLASH_ATTENTION"] = "1"


def calculate_metrics(candidate, reference, transcript=None):
    """
    Calculate various text metrics comparing a candidate summary to a reference summary.

    Parameters:
        candidate (str): The generated summary to be evaluated.
        reference (str): The reference summary to compare against.
        transcript (str, optional): The transcript associated with the summary (not used directly).

    Returns:
        dict: A dictionary containing evaluation metrics such as ROUGE, BERTScore, BLEU, METEOR, length ratio, and redundancy.

    Raises:
        ValueError: If either `candidate` or `reference` is not a string.
    """
    if not isinstance(candidate, str) or not isinstance(reference, str):
        raise ValueError("Candidate and reference must be strings.")

    # Tokenize inputs
    candidate_tokens = candidate.split()
    reference_tokens = [reference.split()]  # Wrap in a list for BLEU and METEOR

    # ROUGE Metrics
    scorer = rouge_scorer.RougeScorer(['rouge1', 'rouge2', 'rougeL'], use_stemmer=True)
    rouge_scores = scorer.score(candidate, reference)

    # BERTScore
    P, R, F1 = score([candidate], [[reference]], lang="en", model_type="bert-base-uncased")

    # BLEU
    chencherry = SmoothingFunction()
    bleu = sentence_bleu(
        reference_tokens,
        candidate_tokens,
        weights=(0.25, 0.25, 0.25, 0.25),
        smoothing_function=chencherry.method1
    )

    # METEOR
    meteor = meteor_score(reference_tokens, candidate_tokens)

    # Length Ratio
    length_ratio = len(candidate_tokens) / len(reference_tokens[0]) if reference_tokens[0] else 0

    # Redundancy
    unique_word_count = len(set(candidate_tokens))
    redundancy = 1 - unique_word_count / len(candidate_tokens) if candidate_tokens else 0

    # Compile metrics into a dictionary
    metrics = {
        "ROUGE-1": rouge_scores["rouge1"].fmeasure,
        "ROUGE-2": rouge_scores["rouge2"].fmeasure,
        "ROUGE-L": rouge_scores["rougeL"].fmeasure,
        "BERT Precision": P.mean().item(),
        "BERT Recall": R.mean().item(),
        "BERT F1": F1.mean().item(),
        "BLEU": bleu,
        "METEOR": meteor,
        "Length Ratio": length_ratio,
        "Redundancy": redundancy
    }
    return metrics


if __name__ == "__main__":
    """
    Entry point for the script. Supports two modes of input:
    - JSON string passed as a command-line argument.
    - JSON file specified with '--file <path>'.

    Outputs:
        - The calculated metrics in JSON format.
        - Error message in JSON format if an exception occurs.
    """
    try:
        # Determine input mode
        if len(sys.argv) > 1 and sys.argv[1] != "--file":
            # API mode: JSON passed as a command-line argument
            input_data = json.loads(sys.argv[1])
        elif len(sys.argv) > 2 and sys.argv[1] == "--file":
            # File mode: JSON read from a file
            file_path = sys.argv[2]
            with open(file_path, "r") as f:
                input_data = json.load(f)
        else:
            raise ValueError("No input provided. Pass a JSON string or use '--file <path>'.")

        # Extract candidate and reference from input data
        candidate = input_data["candidate"]
        reference = input_data["reference"]
        transcript = input_data.get("transcript")

        # Calculate metrics
        results = calculate_metrics(candidate, reference, transcript)

        # Output JSON result to stdout
        print(json.dumps(results, indent=4))

    except Exception as e:
        # Handle exceptions and output error messages
        error_message = {"error": str(e)}
        print(json.dumps(error_message, indent=4))
        sys.exit(1)
