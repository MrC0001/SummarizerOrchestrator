import json
import jsonlines
import os

# Define the enhanced persona and prompt templates
SYSTEM = (
    "<|system|> You are an expert summarizer specializing in call transcripts for fraud departments in banks. "
    "Your task is to produce accurate and concise summaries that exclude conversational elements. "
    "Adhere strictly to the following structure in your summaries:\n\n"
    "1. **Reason for Call:** Briefly describe why the customer is contacting the fraud department.\n"
    "2. **Verification Method:** Detail only the methods used to verify the customer's identity (e.g., asking for date of birth, postcode).\n"
    "3. **Agent Actions Taken:** Outline the specific actions the advisor took during the call (e.g., blocking a payment, escalating the case).\n"
    "4. **Transaction Details:** Provide details of the transaction(s) involved, including date, time, amount, and type.\n\n"
    "Conclude the summary with the token \"<|end|>\". <|end|>"
)

USER = (
    "<|user|> Summarize the following call transcript of a customer contacting the fraud department of a bank. "
    "Transcript: \"{transcript}\" <|end|>"
)

OUTPUT_FILE = "output.jsonl"

def convert_json_to_jsonl(input_json_path, output_jsonl_path):

    try:
        with open(input_json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error reading JSON file '{input_json_path}': {e}")
        return

    with jsonlines.open(output_jsonl_path, mode='w') as writer:
        for entry in data:
            transcript = entry.get("transcript", "").replace('"', '\\"') 
            jsonl_entry = {
                "messages": [
                    {"role": "system", "content": SYSTEM},
                    {"role": "user", "content": USER.format(transcript=transcript)},
                    {"role": "assistant", "content": "<|assistant|> "}
                ]
            }
            writer.write(jsonl_entry)

    print(f"Conversion from JSON to JSONL completed. Output saved to '{output_jsonl_path}'.")



def main():

    input_json_path = "combined_data.json"

    output_jsonl_path = "output.jsonl"

    convert_json_to_jsonl(input_json_path, output_jsonl_path)


if __name__ == "__main__":
    main()
