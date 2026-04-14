from fastapi.testclient import TestClient

from app.main import app, items, _next_id
import app.main as app_module

client = TestClient(app)


def setup_function():
    items.clear()
    app_module._next_id = 1


def test_create_item():
    response = client.post("/items", json={"name": "Widget", "price": 9.99})
    assert response.status_code == 201
    data = response.json()
    assert data["id"] == 1
    assert data["name"] == "Widget"
    assert data["price"] == 9.99


def test_list_items():
    client.post("/items", json={"name": "A", "price": 1.0})
    client.post("/items", json={"name": "B", "price": 2.0})
    response = client.get("/items")
    assert response.status_code == 200
    assert len(response.json()) == 2


def test_get_item():
    client.post("/items", json={"name": "C", "price": 3.0})
    response = client.get("/items/1")
    assert response.status_code == 200


def test_delete_item():
    client.post("/items", json={"name": "D", "price": 4.0})
    response = client.delete("/items/1")
    assert response.status_code == 200
    assert response.json()["status"] == "deleted"


def test_search_items():
    client.post("/items", json={"name": "Apple", "price": 1.0})
    client.post("/items", json={"name": "Banana", "price": 2.0})
    response = client.get("/search?q=apple")
    assert response.status_code == 200
    results = response.json()
    assert len(results) == 1
    assert results[0]["name"] == "Apple"
