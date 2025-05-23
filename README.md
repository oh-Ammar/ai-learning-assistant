# 🧠 AI-Enhanced Quiz Generator

An Android-based educational application powered by AI to **generate quiz questions and summaries** from raw text, documents, or images. Designed for students, educators, and content creators looking to automate assessment creation using NLP and modern machine learning.

---

## 🚀 Features

- 📄 **Text Input** – Paste or type any content to generate quiz questions.
- 🧾 **Document Upload** – Extracts and processes content from PDF and DOCX files.
- 🖼️ **Image OCR** – Converts images to text using Tesseract.
- ✂️ **Summarization** – Uses Hugging Face's `facebook/bart-large-cnn` to condense lengthy text.
- ❓ **Quiz Generation** – Produces multiple-choice questions (easy to hard) using OpenAI's GPT-3.5.
- 📥 **Export as PDF** – Save generated quizzes for offline or print use.

---

## 🛠️ Technologies Used

| Component      | Stack/Tools                               |
|----------------|--------------------------------------------|
| Frontend       | Java (Android Studio)                     |
| OCR            | Tesseract                                 |
| Summarization  | Hugging Face API (`bart-large-cnn`)       |
| Quiz Generation| OpenAI GPT-3.5 via API                    |
| NLP Processing | NLTK, SpaCy (Flask backend)               |
| Backend        | Python (Flask REST API)                   |
| Optional DB    | SQLite / Firebase                         |

---

## 📲 Android Setup

```bash
git clone https://github.com/YOUR_USERNAME/quiz-generator
Open AndroidStudioProject in Android Studio

Sync Gradle and install dependencies

Run on emulator or connected device

🧪 Flask API Setup
bash
Copy code
cd FlaskAPI/
pip install -r requirements.txt
python app.py
ℹ️ API runs on http://10.0.2.2:5000 for Android emulator compatibility.

🔌 API Endpoints
Endpoint	Description
POST /pos_tag_text	Performs POS tagging on input text
POST /generate_questions	Returns multiple-choice questions
POST /summarize_text	Summarizes input text via Hugging Face BART model

📚 Full Documentation
📖 Read the full technical docs on GitBook:
https://your-gitbook-link.gitbook.io

🎥 Demo Video
▶️ Watch it here:
https://your-demo-video-link

📂 Project Structure
rust
Copy code
/AndroidStudioProject      -> Android app source code
/FlaskAPI                  -> Flask backend (NLP, APIs)
/assets                    -> Images, icons, branding
README.md                  -> You're here
👨‍👩‍👧‍👦 Team
Zolo – Android Development, Integration

Teammate #2 – Flask Backend, POS Tagging

Teammate #3 – OCR Pipeline, Testing

📃 License
This project was developed as part of the graduation requirements at
Faculty of Science – Ain Shams University.
