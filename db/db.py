import sqlite3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent

DB_FILE = ROOT / "unit.db"
JSON_FILE = ROOT / "data.json"


def load_json():
    with open(JSON_FILE, "r", encoding="utf-8") as file:
        return json.load(file)


def sync():
    data = load_json()

    connection = sqlite3.connect(DB_FILE)
    cursor = connection.cursor()

    # Create table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS settings (
            key TEXT PRIMARY KEY,
            value TEXT
        )
    """)

    settings = data.get("settings", {})

    for key, value in settings.items():

        cursor.execute("""
            INSERT INTO settings (key, value)
            VALUES (?, ?)

            ON CONFLICT(key)
            DO UPDATE SET value = excluded.value
        """, (key, str(value)))

        print(f"{key} = {value}")

    connection.commit()
    connection.close()

    print("\nUnitDB synchronized.")


sync()
