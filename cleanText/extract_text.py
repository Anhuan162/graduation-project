import re
import ftfy
from unidecode import unidecode
from docx import Document
import mammoth

def extract_docx(path):
    doc = Document(path)
    text = "\n".join([p.text for p in doc.paragraphs])
    return text

def extract_doc(path):
    with open(path, "rb") as f:
        result = mammoth.extract_raw_text(f)
    return result.value

def normalize_text(text):
    # sửa lỗi font, unicode
    text = ftfy.fix_text(text)

    # loại khoảng trắng dư
    text = re.sub(r'\s+', ' ', text)

    return text.strip()

def remove_headers_and_footers(text):
    patterns = [
        r"BỘ THÔNG TIN(.|\n){0,200}Hạnh phúc",
        r"CỘNG HÒA(.|\n){0,200}Hạnh phúc",
        r"Nơi nhận(.|\n)*",
        r"KT\..+",
        r"PHÓ GIÁM ĐỐC(.|\n)*$",
        r"Độc lập – Tự do – Hạnh phúc",
        r"Page \d+",
        r"Số[:\. ]*\d+\/[A-Za-z\-]+"
    ]
    for p in patterns:
        text = re.sub(p, "", text, flags=re.MULTILINE)
    return text

def remove_ocr_garbage(text):
    text = re.sub(r"[•■□►●▪·○◆◇]+", "", text)
    text = re.sub(r"[A-Za-z]*([А-Яа-я]+)[A-Za-z]*", "", text)    # xoá ký tự Cyrillic lỗi OCR
    text = re.sub(r"[^\x00-\x7F]+(?<![àáạảãăâđêếềệểễôơưứừựữử])", "", text)  # ký tự rác
    return text

def fix_line_breaks(text):
    # gộp dòng nếu không kết thúc câu
    text = re.sub(r"(?<![\.!?])\n(?=[a-záàảãạăâéèẻẽẹêíìỉĩịóòỏõọôơúùủũụưđ])",
                  " ",
                  text, flags=re.IGNORECASE)
    # đổi nhiều newline thành 1
    text = re.sub(r"\n{2,}", "\n", text)
    return text

def clean_text(text):
    text = normalize_text(text)
    text = remove_headers_and_footers(text)
    text = remove_ocr_garbage(text)
    text = fix_line_breaks(text)
    return text.strip()
