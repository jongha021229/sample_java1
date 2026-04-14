from fastapi import FastAPI, Query
from pydantic import BaseModel

app = FastAPI(title="sample-java1")


class Item(BaseModel):
    name: str
    price: float
    description: str | None = None


items: dict[int, Item] = {}
_next_id: int = 1


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/items")
def list_items():
    return [{"id": k, **v.model_dump()} for k, v in items.items()]


@app.get("/items/{item_id}")
def get_item(item_id: int):
    if item_id not in items:
        return {"error": "not found"}, 404
    return {"id": item_id, **items[item_id].model_dump()}


@app.post("/items", status_code=201)
def create_item(item: Item):
    global _next_id
    items[_next_id] = item
    result = {"id": _next_id, **item.model_dump()}
    _next_id += 1
    return result


@app.delete("/items/{item_id}")
def delete_item(item_id: int):
    if item_id not in items:
        return {"error": "not found"}, 404
    del items[item_id]
    return {"status": "deleted"}


@app.get("/search")
def search_items(q: str = Query(default="", max_length=100)):
    results = [
        {"id": k, **v.model_dump()}
        for k, v in items.items()
        if q.lower() in v.name.lower()
    ]
    return results
