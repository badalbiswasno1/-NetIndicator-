#!/usr/bin/env python3
import json, os

OFFLINE_DB = {
    "egg": {"name": "Boiled Egg", "emoji": "🥚", "per100g": {"calories": 128, "protein": 12.3, "fat": 8.8, "carbs": 0.2}, "tip": "Boiled egg best! Raw egg er protein 50% absorb hoy, boiled te 90%+"},
    "chicken": {"name": "Chicken Breast", "emoji": "🍗", "per100g": {"calories": 165, "protein": 31, "fat": 3.6, "carbs": 0}, "tip": "Chicken breast = best protein! 100g te 31g protein"},
    "rice": {"name": "White Rice", "emoji": "🍚", "per100g": {"calories": 130, "protein": 2.7, "fat": 0.3, "carbs": 28}, "tip": "Rice = energy source. Post-workout khao for glycogen refill"},
    "fish": {"name": "Salmon Fish", "emoji": "🐟", "per100g": {"calories": 208, "protein": 20, "fat": 13, "carbs": 0}, "tip": "Fish e Omega-3 thake - muscle recovery er jonno best!"},
    "paneer": {"name": "Paneer", "emoji": "🧀", "per100g": {"calories": 265, "protein": 18, "fat": 21, "carbs": 1.2}, "tip": "Paneer = vegetarian protein! 100g te 18g protein"},
    "oats": {"name": "Oats", "emoji": "🌾", "per100g": {"calories": 389, "protein": 16.9, "fat": 6.9, "carbs": 66}, "tip": "Oats = complex carbs. Breakfast e ideal, slow energy release"}
}

HISTORY_FILE = "gym_history.json"
SAVED_FILE = "gym_saved.json"

def load_json(f):
    return json.load(open(f)) if os.path.exists(f) else {}

def save_json(f, d):
    json.dump(d, open(f, "w"), indent=2)

def print_banner():
    print("\n" + "="*50)
    print("  💪 GYM NUTRITION CALCULATOR 💪")
    print("="*50 + "\n")

def show_foods():
    print("📴 OFFLINE FOODS:")
    print("-" * 40)
    for k, v in OFFLINE_DB.items():
        print(f"  {v['emoji']} [{k:8}] {v['name']:20} | 🔥{v['per100g']['calories']:4}kcal | 💪{v['per100g']['protein']:4}g")
    print()

def calculate():
    show_foods()
    choice = input("Select food: ").strip().lower()
    if choice not in OFFLINE_DB:
        print("❌ Invalid!\n")
        return
    food = OFFLINE_DB[choice]
    w = float(input("⚖️ Weight (g, default 100): ") or "100")
    q = int(input("🔢 Qty (default 1): ") or "1")
    r = w / 100
    total = {k: round(v * r * q, 1) for k, v in food["per100g"].items()}
    show_results(food["name"], total, food["tip"])

def show_results(name, total, tip):
    pp = min(round((total["protein"] / 150) * 100), 100)
    cp = min(round((total["calories"] / 2500) * 100), 100)
    print("\n" + "="*50)
    print(f"  🍽️  {name}")
    print(f"  🔥  Calories: {total['calories']:.1f} kcal")
    print(f"  💪  Protein:  {total['protein']:.1f} g")
    print(f"  🥑  Fat:      {total['fat']:.1f} g")
    print(f"  🍞  Carbs:    {total['carbs']:.1f} g")
    print("-"*50)
    print(f"  🎯 Protein: {pp}% | Calories: {cp}%")
    print("="*50)
    print(f"  💡 {tip}")
    print("="*50 + "\n")

def search():
    q = input("\n🔍 Search food: ").strip()
    if not q:
        return
    try:
        import urllib.request, urllib.parse
        url = f"https://api.nal.usda.gov/fdc/v1/foods/search?api_key=DEMO_KEY&query={urllib.parse.quote(q)}&pageSize=3"
        print("🔍 Searching...")
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=10) as r:
            data = json.loads(r.read().decode())
        if data.get("foods"):
            for i, f in enumerate(data["foods"], 1):
                print(f"\n  {i}. {f['description']}")
            c = input("\nSelect (0 to skip): ").strip()
            if c.isdigit() and int(c) > 0:
                food = data["foods"][int(c)-1]
                nut = {}
                for n in food.get("foodNutrients", []):
                    nn = n.get("nutrientName", "")
                    if "Energy" in nn: nut["calories"] = n.get("value", 0)
                    elif "Protein" in nn: nut["protein"] = n.get("value", 0)
                    elif "fat" in nn.lower(): nut["fat"] = n.get("value", 0)
                    elif "Carbohydrate" in nn: nut["carbs"] = n.get("value", 0)
                w = float(input("⚖️ Weight (g, default 100): ") or "100")
                q = int(input("🔢 Qty: ") or "1")
                r = w / 100
                total = {k: round(v * r * q, 1) for k, v in nut.items()}
                show_results(food["description"], total, "Online data from USDA")
        else:
            print("❌ No results!\n")
    except Exception as e:
        print(f"❌ Error: {e}\n")

def main():
    print_banner()
    while True:
        print("1. 📴 Offline Calc")
        print("2. 🌐 Online Search")
        print("3. 🚪 Exit")
        c = input("\nSelect: ").strip()
        if c == "1": calculate()
        elif c == "2": search()
        elif c == "3": print("\n💪 Stay Strong!\n"); break
        else: print("❌ Invalid!\n")

if __name__ == "__main__":
    main()
