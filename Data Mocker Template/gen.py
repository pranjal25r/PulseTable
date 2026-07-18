import csv, random
from datetime import datetime, timedelta

ROWS = 20000
start = datetime(2024, 1, 1)

def aqi(i):
    base = 100 + 30 * (i % 10 >= 5) - 15 * (i % 20 == 0)
    return max(0, base + random.randint(-10, 10))

def pm25(i):
    return min(500, int(50 + (i * 0.05) + random.randint(-10, 10)))

def comp(avg, var=20):
    return max(0, int(avg + random.gauss(0, var)))

with open("synthetic_aqi.csv", "w", newline="") as f:
    w = csv.writer(f)
    w.writerow(["date", "aqi", "pm2_5", "pm10", "no2", "so2", "co", "o3"])
    for i in range(ROWS):
        d = (start + timedelta(minutes=i)).strftime("%Y-%m-%d %H:%M:%S")
        w.writerow([d, aqi(i), pm25(i), comp(80), comp(40), comp(30),
                    round(random.uniform(0.5, 2.0), 2), comp(60)])

print(f"wrote {ROWS} rows to synthetic_aqi.csv")