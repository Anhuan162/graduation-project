import sys
from extract_text import extract_docx, extract_doc, clean_text

def run(path):
    if path.endswith(".docx"):
        raw = extract_docx(path)
    else:
        raw = extract_doc(path)

    cleaned = clean_text(raw)

    output = path + "_clean.txt"
    with open(output, "w", encoding="utf-8") as f:
        f.write(cleaned)

    print("Cleaned file saved:", output)

if __name__ == "__main__":
    run("test.docx")
