import json
import sys
from nltk.translate.bleu_score import sentence_bleu, SmoothingFunction
from nltk.translate.meteor_score import meteor_score
from rouge_score import rouge_scorer
from bert_score import score

def calculate_metrics(candidate, reference, transcript=None):
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
    length_ratio = len(candidate.split()) / len(reference.split()) if reference.split() else 0

    # Redundancy
    words = candidate.split()
    redundancy = 1 - len(set(words)) / len(words) if words else 0

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
    try:
        input_data = json.loads(sys.argv[1])
        candidate = input_data["candidate"]
        reference = input_data["reference"]
        transcript = input_data.get("transcript")

        # Calculate metrics
        results = calculate_metrics(candidate, reference, transcript)

        # Output JSON result to stdout
        print(json.dumps(results))  # Only JSON goes to stdout

    except Exception as e:
        error_message = {"error": str(e)}
        print(json.dumps(error_message), file=sys.stderr)  # Error message to stderr
        sys.exit(1)
