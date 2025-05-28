from flask import Flask, request, jsonify
import spacy
import torch
from transformers import T5ForConditionalGeneration, T5Tokenizer
import numpy as np
import hashlib
from sklearn.metrics.pairwise import cosine_similarity
import re

app = Flask(__name__)

# Load models
nlp = spacy.load("en_core_web_sm")
t5_tokenizer = T5Tokenizer.from_pretrained("t5-small", legacy=False)
t5_model = T5ForConditionalGeneration.from_pretrained("t5-small")

# Stable vector fallback
def get_stable_vector(word):
    h = hashlib.sha256(word.lower().encode()).digest()
    np.random.seed(int.from_bytes(h[:4], 'little'))
    return np.random.rand(300)

def get_vector(word):
    try:
        vec = nlp.vocab.get_vector(word.lower())
        if vec is not None and len(vec) == 300:
            return vec
        return get_stable_vector(word)
    except:
        return get_stable_vector(word)

@app.route("/pos_tag_text", methods=["POST"])
def pos_tag_text():
    data = request.get_json()
    text = data.get("text", "")
    doc = nlp(text)
    tagged = [{"text": token.text, "pos": token.pos_} for token in doc if not token.is_space]
    return jsonify(tagged)

@app.route("/check_answers", methods=["POST"])
def check_answers():
    data = request.get_json()
    user_answers = data.get("user_answers", [])
    correct_answers = data.get("correct_answers", [])

    results = []
    for user_ans, correct_ans in zip(user_answers, correct_answers):
        user_doc = nlp(user_ans)
        correct_doc = nlp(correct_ans)

        user_vectors = [get_vector(token.text) for token in user_doc if token.text.strip()]
        correct_vectors = [get_vector(token.text) for token in correct_doc if token.text.strip()]

        if not user_vectors or not correct_vectors:
            similarity = 0.0
            is_correct = False
        else:
            user_vec = np.mean(user_vectors, axis=0)
            correct_vec = np.mean(correct_vectors, axis=0)
            similarity = float(cosine_similarity([user_vec], [correct_vec])[0][0])
            is_correct = similarity >= 0.75

        results.append({
            "user_answer": user_ans,
            "correct_answer": correct_ans,
            "similarity": similarity,
            "is_correct": is_correct
        })

    return jsonify(results)

def is_valid_sentence(s):
    return (
        8 < len(s.split()) < 30 and
        not re.search(r"[{}=;<>]", s) and
        not s.lower().startswith(("example", "fig", "note", "table", "https"))
    )

def is_valid_question(q):
    if len(q.split()) < 4:
        return False
    if "does it" in q.lower() or "what does it" in q.lower():
        return False
    if not q.endswith("?"):
        return False
    return True

def generate_question_t5(sentence):
    prompt = f"generate question: {sentence}"
    input_ids = t5_tokenizer.encode(prompt, return_tensors="pt", max_length=512, truncation=True)
    output_ids = t5_model.generate(input_ids, max_length=64, num_beams=4, early_stopping=True)
    return t5_tokenizer.decode(output_ids[0], skip_special_tokens=True)

@app.route("/generate_quiz", methods=["POST"])
def generate_quiz():
    data = request.get_json()
    text = data.get("text", "").strip()
    if not text:
        return jsonify([])

    print("✅ /generate_quiz called")

    doc = nlp(text)
    questions = []

    for sent in doc.sents:
        sent_text = sent.text.strip()
        if not is_valid_sentence(sent_text):
            continue
        try:
            q = generate_question_t5(sent_text)
            if not q.endswith("?"):
                q = q.strip().rstrip(".") + "?"
            if is_valid_question(q):
                questions.append({"question": q, "answer": sent_text})
        except Exception as e:
            print(f"❌ Error generating question: {e}")
            continue

    return jsonify(questions)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
