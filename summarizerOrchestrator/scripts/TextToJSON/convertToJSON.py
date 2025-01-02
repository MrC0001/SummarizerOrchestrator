import os
import json
import re

# Directory containing your text files
directory = "./transcripts"

# This list will hold the JSON objects
data_list = []

# Iterate through each file in the directory
for filename in os.listdir(directory):
    if filename.endswith(".txt"):
        filepath = os.path.join(directory, filename)
        
        # Read the file content
        with open(filepath, "r", encoding="utf-8") as f:
            transcript = f.read().strip()

        # Derive scenario name from filename (remove .txt and replace underscores if needed)
        scenario_name = os.path.splitext(filename)[0]
        # If you want to replace underscores with spaces or do some formatting:
        # scenario_name = scenario_name.replace("_", " ")
        scenario_name = re.sub(r'^\d+_', '', scenario_name)

        # Create the object
        data_list.append({
            "scenario": scenario_name,
            "transcript": transcript
        })

# Convert the list to JSON and write to an output file
with open("combined_data.json", "w", encoding="utf-8") as out_file:
    json.dump(data_list, out_file, indent=2, ensure_ascii=False)

print("JSON file created: combined_data.json")
