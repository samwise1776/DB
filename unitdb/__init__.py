import sqlite3


class UnitDB:

    @staticmethod
    def getsql():
        connection = sqlite3.connect("../db/unit.db")
        cursor = connection.cursor()

        cursor.execute("""
            SELECT name, sql
            FROM sqlite_master
            WHERE type IN ('table', 'index', 'trigger', 'view')
            AND sql IS NOT NULL
        """)

        data = cursor.fetchall()

        connection.close()

        return data
