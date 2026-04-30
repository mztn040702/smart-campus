# AI Recommend Service

This service provides semantic scoring for the Smart Campus recommendation module.

## Model

- Default model: `paraphrase-multilingual-MiniLM-L12-v2`
- Purpose: multilingual and Chinese-friendly semantic matching for recommendation ranking

## Setup

```bash
cd ai-recommend-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## Run

```bash
uvicorn app:app --host 0.0.0.0 --port 8001
```

## Health Check

```bash
curl http://localhost:8001/health
```

## Score API

`POST /score`

Request body:

```json
{
  "userProfileText": "user interest in product: java books. spring boot. internship.",
  "items": [
    { "id": "1", "text": "Java design patterns | books | classic textbook" },
    { "id": "2", "text": "MacBook Pro | electronics | laptop for campus study" }
  ]
}
```

Response body:

```json
{
  "model": "paraphrase-multilingual-MiniLM-L12-v2",
  "scores": [
    { "itemId": "1", "semanticScore": 0.92 },
    { "itemId": "2", "semanticScore": 0.27 }
  ]
}
```

## Notes

- The Spring Boot backend will automatically fall back to legacy recommendation logic if this service is unavailable or returns an error.
- Start this service manually during development, demo, or thesis defense.
