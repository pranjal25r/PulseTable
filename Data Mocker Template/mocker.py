import random
import time

# Pool of names and surnames
first_names = ["Susan", "John", "Alice", "Bob", "Charlie", "Emma", "David"]
surnames = ["Woodworth", "Smith", "Johnson", "Williams", "Brown", "Davis", "Miller"]

# File path to write names
file_path = r"..\text.txt"

# Continuous writing loop
while True:
    # Choose a random first name and surname
    first_name = random.choice(first_names)
    surname = random.choice(surnames)

    # Combine the chosen name and surname
    full_name = f"{first_name};{surname}"

    # Write the result to the text file
    with open(file_path, "a") as file:
        file.write(full_name + "\n")

    # Print the name to the console (optional)
    print(f"Written: {full_name}")

    # Wait for 1 second before writing again
    time.sleep(2)
