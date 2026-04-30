from typing import List

from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


MODEL_NAME = "paraphrase-multilingual-MiniLM-L12-v2"
model = SentenceTransformer(MODEL_NAME)
app = FastAPI(title="smart-campus-ai-recommend")


class ScoreItem(BaseModel):
    id: str
    text: str


class ScoreRequest(BaseModel):
    userProfileText: str
    items: List[ScoreItem]


class ScoreResult(BaseModel):
    itemId: str
    semanticScore: float


class ScoreResponse(BaseModel):
    model: str
    scores: List[ScoreResult]


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "model": MODEL_NAME}


@app.post("/score", response_model=ScoreResponse)
def score(request: ScoreRequest) -> ScoreResponse:
    if not request.items:
        return ScoreResponse(model=MODEL_NAME, scores=[])

    profile_embedding = model.encode([request.userProfileText], normalize_embeddings=True)
    item_embeddings = model.encode(
        [item.text for item in request.items],
        normalize_embeddings=True
    )
    similarities = cosine_similarity(profile_embedding, item_embeddings)[0]

    scores = [
        ScoreResult(itemId=item.id, semanticScore=float(max(0.0, min(1.0, score))))
        for item, score in zip(request.items, similarities)
    ]
    return ScoreResponse(model=MODEL_NAME, scores=scores)
