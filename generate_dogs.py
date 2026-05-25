#!/usr/bin/env python3
"""Download one image per breed and generate dogs.json from labels.txt."""
import os
import json
import time
import zipfile
import sys

try:
    import requests
except ImportError:
    print("requests not installed. Installing...")
    os.system(f"{sys.executable} -m pip install requests")
    import requests

BASE_DIR = "/Users/sinue/Documents/Fruse/Dogedex"
LABELS_FILE = f"{BASE_DIR}/app/src/main/assets/labels.txt"
IMAGES_DIR = f"{BASE_DIR}/images"
JSON_OUTPUT = f"{BASE_DIR}/dogs.json"
ZIP_OUTPUT = f"{BASE_DIR}/dog_images.zip"

os.makedirs(IMAGES_DIR, exist_ok=True)

# breed_key → dog.ceo API path (breed or breed/sub)
# None means fall back to Wikipedia
DOG_CEO_BREEDS = {
    "chihuahua": "chihuahua",
    "japanese_spaniel": "spaniel/japanese",
    "maltese_dog": "maltese",
    "pekinese": "pekinese",
    "shih-tzu": "shihtzu",
    "blenheim_spaniel": "spaniel/blenheim",
    "papillon": "papillon",
    "toy_terrier": None,
    "rhodesian_ridgeback": "ridgeback/rhodesian",
    "afghan_hound": "hound/afghan",
    "basset": "basset",
    "beagle": "beagle",
    "bloodhound": "bloodhound",
    "bluetick": "bluetick",
    "black-and-tan_coonhound": "hound/black",
    "walker_hound": "hound/walker",
    "english_foxhound": "hound/english",
    "redbone": "hound/redbone",
    "borzoi": "borzoi",
    "irish_wolfhound": "wolfhound/irish",
    "italian_greyhound": "greyhound/italian",
    "whippet": "whippet",
    "ibizan_hound": "hound/ibizan",
    "norwegian_elkhound": "elkhound/norwegian",
    "otterhound": "otterhound",
    "saluki": "saluki",
    "scottish_deerhound": "deerhound/scottish",
    "weimaraner": "weimaraner",
    "staffordshire_bullterrier": "terrier/staffordshire",
    "american_staffordshire_terrier": "terrier/american",
    "bedlington_terrier": "terrier/bedlington",
    "border_terrier": "terrier/border",
    "kerry_blue_terrier": "terrier/kerry",
    "irish_terrier": "terrier/irish",
    "norfolk_terrier": "terrier/norfolk",
    "norwich_terrier": "terrier/norwich",
    "yorkshire_terrier": "terrier/yorkshire",
    "wire-haired_fox_terrier": "terrier/fox",
    "lakeland_terrier": "terrier/lakeland",
    "sealyham_terrier": "terrier/sealyham",
    "airedale": "airedale",
    "cairn": "cairn",
    "australian_terrier": "terrier/australian",
    "dandie_dinmont": "terrier/dandie",
    "boston_bull": "terrier/boston",
    "miniature_schnauzer": "schnauzer/miniature",
    "giant_schnauzer": "schnauzer/giant",
    "standard_schnauzer": "schnauzer/standard",
    "scotch_terrier": "terrier/scotch",
    "tibetan_terrier": "terrier/tibetan",
    "silky_terrier": "terrier/silky",
    "soft-coated_wheaten_terrier": "terrier/wheaten",
    "west_highland_white_terrier": "terrier/westhighland",
    "lhasa": "lhasa",
    "flat-coated_retriever": "retriever/flatcoated",
    "curly-coated_retriever": "retriever/curly",
    "golden_retriever": "retriever/golden",
    "labrador_retriever": "retriever/labrador",
    "chesapeake_bay_retriever": "retriever/chesapeake",
    "german_short-haired_pointer": "pointer/german",
    "vizsla": "vizsla",
    "english_setter": "setter/english",
    "irish_setter": "setter/irish",
    "gordon_setter": "setter/gordon",
    "brittany_spaniel": "spaniel/brittany",
    "clumber": "spaniel/clumber",
    "english_springer": "spaniel/springer",
    "welsh_springer_spaniel": "spaniel/welsh",
    "cocker_spaniel": "spaniel/cocker",
    "sussex_spaniel": "spaniel/sussex",
    "irish_water_spaniel": "spaniel/irish",
    "kuvasz": "kuvasz",
    "schipperke": "schipperke",
    "groenendael": "groenendael",
    "malinois": "malinois",
    "briard": "briard",
    "kelpie": "kelpie",
    "komondor": "komondor",
    "old_english_sheepdog": "sheepdog/english",
    "shetland_sheepdog": "sheepdog/shetland",
    "collie": "collie",
    "border_collie": "collie/border",
    "bouvier_des_flandres": "bouvier/flandres",
    "rottweiler": "rottweiler",
    "german_shepherd": "germanshepherd",
    "doberman": "doberman",
    "miniature_pinscher": "pinscher/miniature",
    "greater_swiss_mountain_dog": "mountain/swiss",
    "bernese_mountain_dog": "mountain/bernese",
    "appenzeller": "appenzeller",
    "entlebucher": "entlebucher",
    "boxer": "boxer",
    "bull_mastiff": "mastiff/bull",
    "tibetan_mastiff": "mastiff/tibetan",
    "french_bulldog": "bulldog/french",
    "great_dane": "dane/great",
    "saint_bernard": "stbernard",
    "eskimo_dog": "eskimo",
    "malamute": "malamute",
    "siberian_husky": "husky",
    "affenpinscher": "affenpinscher",
    "basenji": "basenji",
    "pug": "pug",
    "leonberg": "leonberg",
    "newfoundland": "newfoundland",
    "great_pyrenees": "pyrenees",
    "samoyed": "samoyed",
    "pomeranian": "pomeranian",
    "chow": "chow",
    "keeshond": "keeshond",
    "brabancon_griffon": "griffon/brussels",
    "pembroke": "corgi/pembroke",
    "cardigan": "corgi/cardigan",
    "toy_poodle": "poodle/toy",
    "miniature_poodle": "poodle/miniature",
    "standard_poodle": "poodle/standard",
    "mexican_hairless": "mexicanhairless",
    "dingo": "dingo",
    "dhole": None,
    "african_hunting_dog": None,
}

# Wikipedia article title for breeds not in dog.ceo
WIKIPEDIA_TITLES = {
    "toy_terrier": "English_Toy_Terrier",
    "black-and-tan_coonhound": "Black_and_Tan_Coonhound",
    "dhole": "Dhole",
    "african_hunting_dog": "African_wild_dog",
    "blenheim_spaniel": "Cavalier_King_Charles_Spaniel",
    "bluetick": "Bluetick_Coonhound",
    "brabancon_griffon": "Brussels_Griffon",
    "great_pyrenees": "Great_Pyrenees",
    "eskimo_dog": "Canadian_Eskimo_Dog",
    "leonberg": "Leonberger",
    "lhasa": "Lhasa_Apso",
    "scotch_terrier": "Scottish_Terrier",
    "boston_bull": "Boston_Terrier",
    "bull_mastiff": "Bullmastiff",
    "mexican_hairless": "Xoloitzcuintli",
}

# (display_name, type, height_female_cm, height_male_cm, life_expectancy, temperament, weight_female_kg, weight_male_kg)
BREED_INFO = {
    "chihuahua": ("Chihuahua", "Toy", 15, 20, "12-20", "Courageous, Alert, Confident", 1.0, 2.7),
    "japanese_spaniel": ("Japanese Chin", "Toy", 20, 23, "10-12", "Intelligent, Loyal, Alert", 2.3, 3.6),
    "maltese_dog": ("Maltese", "Toy", 21, 25, "12-15", "Gentle, Responsive, Playful", 1.8, 3.2),
    "pekinese": ("Pekingese", "Toy", 15, 23, "12-15", "Opinionated, Affectionate, Loyal", 3.0, 5.0),
    "shih-tzu": ("Shih Tzu", "Toy", 20, 28, "10-16", "Lively, Courageous, Alert", 4.0, 7.0),
    "blenheim_spaniel": ("Blenheim Spaniel", "Toy", 30, 33, "9-15", "Playful, Patient, Adaptable", 5.0, 8.0),
    "papillon": ("Papillon", "Toy", 20, 28, "13-15", "Alert, Energetic, Intelligent", 2.0, 4.5),
    "toy_terrier": ("Toy Terrier", "Toy", 25, 30, "12-15", "Active, Loyal, Courageous", 2.0, 3.5),
    "rhodesian_ridgeback": ("Rhodesian Ridgeback", "Hound", 61, 69, "10-12", "Dignified, Strong-willed, Reserved", 29.0, 41.0),
    "afghan_hound": ("Afghan Hound", "Hound", 60, 74, "12-14", "Aloof, Clownish, Dignified", 22.0, 27.0),
    "basset": ("Basset Hound", "Hound", 28, 38, "10-12", "Friendly, Outgoing, Playful", 20.0, 29.0),
    "beagle": ("Beagle", "Hound", 33, 38, "12-15", "Merry, Friendly, Curious", 9.0, 11.0),
    "bloodhound": ("Bloodhound", "Hound", 58, 69, "10-12", "Stubborn, Affectionate, Gentle", 36.0, 50.0),
    "bluetick": ("Bluetick Coonhound", "Hound", 53, 69, "11-12", "Loyal, Friendly, Alert", 18.0, 36.0),
    "black-and-tan_coonhound": ("Black and Tan Coonhound", "Hound", 58, 69, "10-12", "Trusting, Gentle, Easygoing", 25.0, 34.0),
    "walker_hound": ("Treeing Walker Coonhound", "Hound", 51, 69, "12-13", "Intelligent, Courageous, Alert", 22.0, 32.0),
    "english_foxhound": ("English Foxhound", "Hound", 53, 64, "10-13", "Gentle, Sociable, Active", 25.0, 34.0),
    "redbone": ("Redbone Coonhound", "Hound", 53, 66, "12-15", "Friendly, Loyal, Active", 18.0, 32.0),
    "borzoi": ("Borzoi", "Hound", 68, 84, "9-14", "Gentle, Athletic, Quiet", 27.0, 48.0),
    "irish_wolfhound": ("Irish Wolfhound", "Hound", 71, 86, "6-10", "Generous, Thoughtful, Patient", 48.0, 70.0),
    "italian_greyhound": ("Italian Greyhound", "Toy", 33, 38, "14-15", "Mischievous, Alert, Companionable", 3.5, 5.0),
    "whippet": ("Whippet", "Hound", 44, 51, "12-15", "Gentle, Affectionate, Lively", 12.5, 14.0),
    "ibizan_hound": ("Ibizan Hound", "Hound", 57, 74, "12-14", "Adaptable, Trainable, Energetic", 18.0, 29.0),
    "norwegian_elkhound": ("Norwegian Elkhound", "Hound", 49, 52, "12-15", "Friendly, Reliable, Playful", 19.0, 23.0),
    "otterhound": ("Otterhound", "Hound", 61, 69, "10-13", "Boisterous, Even-tempered, Friendly", 30.0, 52.0),
    "saluki": ("Saluki", "Hound", 58, 71, "12-14", "Reserved, Aloof, Devoted", 18.0, 27.0),
    "scottish_deerhound": ("Scottish Deerhound", "Hound", 71, 81, "8-11", "Docile, Friendly, Dignified", 36.0, 50.0),
    "weimaraner": ("Weimaraner", "Sporting", 57, 70, "11-14", "Alert, Friendly, Fearless", 23.0, 32.0),
    "staffordshire_bullterrier": ("Staffordshire Bull Terrier", "Terrier", 33, 41, "12-14", "Loyal, Brave, Tenacious", 11.0, 17.0),
    "american_staffordshire_terrier": ("American Staffordshire Terrier", "Terrier", 43, 48, "12-16", "Loyal, Courageous, Devoted", 25.0, 30.0),
    "bedlington_terrier": ("Bedlington Terrier", "Terrier", 38, 43, "14-16", "Loyal, Spirited, Good-tempered", 8.0, 10.5),
    "border_terrier": ("Border Terrier", "Terrier", 28, 33, "12-15", "Affectionate, Alert, Obedient", 5.0, 7.0),
    "kerry_blue_terrier": ("Kerry Blue Terrier", "Terrier", 44, 51, "13-15", "Spirited, Alert, Loyal", 15.0, 20.0),
    "irish_terrier": ("Irish Terrier", "Terrier", 43, 46, "13-15", "Lively, Dominant, Respectful", 11.0, 12.0),
    "norfolk_terrier": ("Norfolk Terrier", "Terrier", 23, 26, "12-15", "Alert, Fearless, Fun-loving", 4.5, 5.0),
    "norwich_terrier": ("Norwich Terrier", "Terrier", 23, 26, "12-15", "Alert, Companionable, Fearless", 4.5, 5.5),
    "yorkshire_terrier": ("Yorkshire Terrier", "Toy", 18, 23, "13-20", "Bold, Independent, Confident", 1.8, 3.2),
    "wire-haired_fox_terrier": ("Wire Fox Terrier", "Terrier", 36, 39, "12-15", "Alert, Gregarious, Active", 6.8, 8.2),
    "lakeland_terrier": ("Lakeland Terrier", "Terrier", 33, 37, "12-15", "Bold, Friendly, Confident", 6.8, 7.7),
    "sealyham_terrier": ("Sealyham Terrier", "Terrier", 25, 30, "12-14", "Calm, Outgoing, Alert", 8.0, 9.0),
    "airedale": ("Airedale Terrier", "Terrier", 56, 61, "11-14", "Outgoing, Friendly, Confident", 18.0, 29.0),
    "cairn": ("Cairn Terrier", "Terrier", 28, 30, "12-15", "Hardy, Active, Fearless", 5.4, 6.4),
    "australian_terrier": ("Australian Terrier", "Terrier", 23, 28, "11-15", "Spirited, Alert, Loyal", 5.5, 6.5),
    "dandie_dinmont": ("Dandie Dinmont Terrier", "Terrier", 20, 28, "12-15", "Independent, Determined, Lively", 8.0, 11.0),
    "boston_bull": ("Boston Terrier", "Non-Sporting", 38, 43, "11-15", "Friendly, Lively, Intelligent", 5.0, 11.0),
    "miniature_schnauzer": ("Miniature Schnauzer", "Terrier", 30, 36, "12-15", "Fearless, Lively, Alert", 5.0, 8.0),
    "giant_schnauzer": ("Giant Schnauzer", "Working", 58, 70, "12-15", "Loyal, Intelligent, Spirited", 25.0, 48.0),
    "standard_schnauzer": ("Standard Schnauzer", "Working", 43, 50, "13-16", "Lively, Playful, Alert", 14.0, 20.0),
    "scotch_terrier": ("Scottish Terrier", "Terrier", 25, 28, "11-13", "Alert, Spirited, Independent", 8.5, 10.5),
    "tibetan_terrier": ("Tibetan Terrier", "Non-Sporting", 36, 43, "12-15", "Loyal, Gentle, Energetic", 8.0, 14.0),
    "silky_terrier": ("Silky Terrier", "Toy", 23, 25, "12-15", "Alert, Inquisitive, Friendly", 3.5, 5.0),
    "soft-coated_wheaten_terrier": ("Soft-Coated Wheaten Terrier", "Terrier", 43, 49, "12-15", "Energetic, Loyal, Playful", 13.5, 20.5),
    "west_highland_white_terrier": ("West Highland White Terrier", "Terrier", 25, 28, "12-16", "Hardy, Alert, Friendly", 6.0, 10.0),
    "lhasa": ("Lhasa Apso", "Non-Sporting", 25, 28, "12-15", "Assertive, Loyal, Obedient", 5.0, 8.0),
    "flat-coated_retriever": ("Flat-Coated Retriever", "Sporting", 56, 62, "10-13", "Good-natured, Cheerful, Devoted", 25.0, 36.0),
    "curly-coated_retriever": ("Curly-Coated Retriever", "Sporting", 58, 69, "9-14", "Lively, Trainable, Perceptive", 25.0, 32.0),
    "golden_retriever": ("Golden Retriever", "Sporting", 55, 61, "10-12", "Friendly, Reliable, Trustworthy", 25.0, 34.0),
    "labrador_retriever": ("Labrador Retriever", "Sporting", 54, 62, "10-14", "Kind, Outgoing, Trusting", 25.0, 36.0),
    "chesapeake_bay_retriever": ("Chesapeake Bay Retriever", "Sporting", 53, 66, "10-13", "Affectionate, Intelligent, Brave", 25.0, 36.0),
    "german_short-haired_pointer": ("German Shorthaired Pointer", "Sporting", 53, 64, "12-14", "Cooperative, Intelligent, Boisterous", 20.0, 32.0),
    "vizsla": ("Vizsla", "Sporting", 53, 64, "12-15", "Affectionate, Loyal, Energetic", 18.0, 29.0),
    "english_setter": ("English Setter", "Sporting", 61, 69, "12-14", "Gentle, Friendly, Mischievous", 20.0, 36.0),
    "irish_setter": ("Irish Setter", "Sporting", 60, 67, "12-15", "Lively, Energetic, Playful", 25.0, 32.0),
    "gordon_setter": ("Gordon Setter", "Sporting", 60, 69, "12-13", "Alert, Loyal, Confident", 20.0, 36.0),
    "brittany_spaniel": ("Brittany", "Sporting", 46, 52, "12-14", "Adaptable, Agile, Attentive", 14.0, 20.0),
    "clumber": ("Clumber Spaniel", "Sporting", 43, 51, "10-12", "Loyal, Affectionate, Calm", 25.0, 39.0),
    "english_springer": ("English Springer Spaniel", "Sporting", 46, 51, "12-14", "Friendly, Playful, Obedient", 18.0, 25.0),
    "welsh_springer_spaniel": ("Welsh Springer Spaniel", "Sporting", 43, 48, "12-15", "Active, Stubborn, Loyal", 16.0, 20.0),
    "cocker_spaniel": ("Cocker Spaniel", "Sporting", 36, 38, "12-15", "Gentle, Smart, Happy", 11.0, 14.5),
    "sussex_spaniel": ("Sussex Spaniel", "Sporting", 33, 41, "13-15", "Loyal, Friendly, Even-tempered", 16.0, 20.0),
    "irish_water_spaniel": ("Irish Water Spaniel", "Sporting", 53, 60, "10-12", "Loyal, Hardworking, Intelligent", 20.0, 30.0),
    "kuvasz": ("Kuvasz", "Working", 66, 76, "10-12", "Loyal, Patient, Protective", 32.0, 52.0),
    "schipperke": ("Schipperke", "Non-Sporting", 25, 33, "13-15", "Alert, Curious, Energetic", 3.0, 9.0),
    "groenendael": ("Belgian Sheepdog", "Herding", 56, 66, "12-14", "Devoted, Alert, Intelligent", 20.0, 30.0),
    "malinois": ("Belgian Malinois", "Herding", 56, 66, "12-14", "Alert, Confident, Hardworking", 18.0, 29.0),
    "briard": ("Briard", "Herding", 56, 69, "10-12", "Loyal, Fearless, Obedient", 25.0, 41.0),
    "kelpie": ("Australian Kelpie", "Herding", 43, 51, "10-13", "Alert, Energetic, Loyal", 11.0, 20.0),
    "komondor": ("Komondor", "Working", 64, 80, "10-12", "Loyal, Steady, Brave", 36.0, 59.0),
    "old_english_sheepdog": ("Old English Sheepdog", "Herding", 56, 61, "10-12", "Adaptable, Gentle, Smart", 27.0, 45.0),
    "shetland_sheepdog": ("Shetland Sheepdog", "Herding", 33, 40, "12-14", "Lively, Alert, Intelligent", 5.4, 12.0),
    "collie": ("Collie", "Herding", 56, 66, "12-14", "Devoted, Graceful, Intelligent", 23.0, 34.0),
    "border_collie": ("Border Collie", "Herding", 46, 56, "12-15", "Energetic, Alert, Responsive", 14.0, 20.0),
    "bouvier_des_flandres": ("Bouvier des Flandres", "Herding", 59, 68, "10-12", "Intelligent, Rational, Gentle", 27.0, 54.0),
    "rottweiler": ("Rottweiler", "Working", 55, 69, "8-11", "Loyal, Loving, Confident", 35.0, 60.0),
    "german_shepherd": ("German Shepherd", "Herding", 55, 65, "9-13", "Loyal, Courageous, Obedient", 22.0, 40.0),
    "doberman": ("Doberman Pinscher", "Working", 63, 72, "10-12", "Alert, Loyal, Fearless", 27.0, 45.0),
    "miniature_pinscher": ("Miniature Pinscher", "Toy", 25, 30, "12-16", "Energetic, Outgoing, Alert", 3.5, 4.5),
    "greater_swiss_mountain_dog": ("Greater Swiss Mountain Dog", "Working", 60, 72, "8-11", "Devoted, Alert, Strong-willed", 36.0, 68.0),
    "bernese_mountain_dog": ("Bernese Mountain Dog", "Working", 58, 70, "7-10", "Affectionate, Loyal, Gentle", 36.0, 52.0),
    "appenzeller": ("Appenzeller Sennenhund", "Working", 47, 56, "12-14", "Alert, Reliable, Energetic", 22.0, 32.0),
    "entlebucher": ("Entlebucher Mountain Dog", "Working", 40, 50, "11-13", "Lively, Alert, Loyal", 20.0, 30.0),
    "boxer": ("Boxer", "Working", 53, 63, "10-12", "Playful, Loyal, Friendly", 25.0, 32.0),
    "bull_mastiff": ("Bullmastiff", "Working", 61, 69, "7-9", "Reliable, Devoted, Alert", 41.0, 59.0),
    "tibetan_mastiff": ("Tibetan Mastiff", "Working", 61, 71, "10-14", "Loyal, Aloof, Protective", 34.0, 82.0),
    "french_bulldog": ("French Bulldog", "Non-Sporting", 25, 31, "10-12", "Playful, Affectionate, Easygoing", 8.0, 12.5),
    "great_dane": ("Great Dane", "Working", 71, 86, "7-10", "Friendly, Patient, Dependable", 45.0, 91.0),
    "saint_bernard": ("Saint Bernard", "Working", 65, 90, "8-10", "Playful, Calm, Gentle", 54.0, 91.0),
    "eskimo_dog": ("Canadian Eskimo Dog", "Working", 50, 70, "12-15", "Alert, Tough, Loyal", 18.0, 40.0),
    "malamute": ("Alaskan Malamute", "Working", 58, 64, "10-14", "Affectionate, Loyal, Playful", 34.0, 39.0),
    "siberian_husky": ("Siberian Husky", "Working", 51, 60, "12-15", "Outgoing, Gentle, Alert", 16.0, 27.0),
    "affenpinscher": ("Affenpinscher", "Toy", 23, 28, "12-14", "Stubborn, Curious, Alert", 2.9, 4.5),
    "basenji": ("Basenji", "Hound", 40, 43, "13-14", "Alert, Affectionate, Curious", 9.5, 11.0),
    "pug": ("Pug", "Toy", 25, 30, "12-15", "Docile, Charming, Stubborn", 6.3, 8.1),
    "leonberg": ("Leonberger", "Working", 65, 80, "8-9", "Loyal, Obedient, Fearless", 41.0, 75.0),
    "newfoundland": ("Newfoundland", "Working", 63, 74, "8-10", "Sweet-tempered, Gentle, Patient", 45.0, 68.0),
    "great_pyrenees": ("Great Pyrenees", "Working", 65, 82, "10-12", "Strong-willed, Confident, Gentle", 36.0, 54.0),
    "samoyed": ("Samoyed", "Working", 48, 60, "12-14", "Lively, Stubborn, Playful", 17.0, 30.0),
    "pomeranian": ("Pomeranian", "Toy", 18, 28, "12-16", "Sociable, Lively, Bold", 1.9, 3.5),
    "chow": ("Chow Chow", "Non-Sporting", 43, 51, "9-15", "Loyal, Independent, Quiet", 20.0, 32.0),
    "keeshond": ("Keeshond", "Non-Sporting", 43, 48, "12-15", "Alert, Playful, Agile", 16.0, 20.0),
    "brabancon_griffon": ("Brussels Griffon", "Toy", 18, 25, "12-15", "Alert, Curious, Loyal", 3.6, 4.5),
    "pembroke": ("Pembroke Welsh Corgi", "Herding", 25, 30, "12-14", "Playful, Bold, Outgoing", 11.0, 14.0),
    "cardigan": ("Cardigan Welsh Corgi", "Herding", 25, 33, "12-15", "Alert, Loyal, Affectionate", 11.0, 17.0),
    "toy_poodle": ("Toy Poodle", "Toy", 24, 28, "14-18", "Alert, Active, Intelligent", 3.0, 4.5),
    "miniature_poodle": ("Miniature Poodle", "Non-Sporting", 28, 38, "14-17", "Alert, Active, Intelligent", 5.0, 9.0),
    "standard_poodle": ("Standard Poodle", "Non-Sporting", 45, 62, "11-15", "Alert, Active, Intelligent", 18.0, 32.0),
    "mexican_hairless": ("Xoloitzcuintli", "Non-Sporting", 25, 58, "13-18", "Alert, Loyal, Calm", 4.0, 20.0),
    "dingo": ("Dingo", "Foundation Stock", 44, 63, "5-15", "Alert, Agile, Loyal", 13.0, 20.0),
    "dhole": ("Dhole", "Wild Canid", 42, 55, "10-13", "Alert, Playful, Social", 10.0, 21.0),
    "african_hunting_dog": ("African Wild Dog", "Wild Canid", 60, 75, "10-12", "Alert, Social, Energetic", 18.0, 36.0),
}


def dog_ceo_image_url(api_path: str) -> str | None:
    """Return a random image URL from dog.ceo for the given breed path."""
    parts = api_path.split("/")
    if len(parts) == 1:
        url = f"https://dog.ceo/api/breed/{parts[0]}/images/random"
    elif len(parts) == 2:
        url = f"https://dog.ceo/api/breed/{parts[0]}/{parts[1]}/images/random"
    else:
        url = f"https://dog.ceo/api/breed/{parts[0]}/{parts[1]}/images/random"

    try:
        r = requests.get(url, timeout=10)
        if r.status_code == 200:
            data = r.json()
            if data.get("status") == "success":
                return data["message"]
    except Exception as e:
        print(f"    dog.ceo error ({api_path}): {e}")
    return None


def wikipedia_image_url(title: str) -> str | None:
    """Return thumbnail URL from Wikipedia page summary."""
    url = f"https://en.wikipedia.org/api/rest_v1/page/summary/{title}"
    try:
        r = requests.get(url, timeout=10, headers={"User-Agent": "DogedexBot/1.0"})
        if r.status_code == 200:
            data = r.json()
            thumb = data.get("thumbnail", {})
            if thumb.get("source"):
                return thumb["source"]
    except Exception as e:
        print(f"    Wikipedia error ({title}): {e}")
    return None


def download_image(url: str, path: str) -> bool:
    try:
        r = requests.get(url, timeout=30, stream=True,
                         headers={"User-Agent": "DogedexBot/1.0"})
        if r.status_code == 200:
            with open(path, "wb") as f:
                for chunk in r.iter_content(8192):
                    f.write(chunk)
            return True
    except Exception as e:
        print(f"    Download error: {e}")
    return False


def parse_labels(path: str):
    breeds = []
    with open(path) as f:
        for idx, line in enumerate(f, start=1):
            line = line.strip()
            if not line:
                continue
            label = line  # e.g. n02085620-chihuahua
            nid, breed_name = label.split("-", 1)
            numeric_id = int(nid[1:])  # strip leading 'n'
            breeds.append(dict(index=idx, nid=nid, label=label,
                               breed_name=breed_name, numeric_id=numeric_id))
    return breeds


def main():
    breeds = parse_labels(LABELS_FILE)
    print(f"Parsed {len(breeds)} breeds\n")

    dogs_json = []
    failed = []

    for b in breeds:
        idx = b["index"]
        breed_name = b["breed_name"]
        nid = b["nid"]
        label = b["label"]
        numeric_id = b["numeric_id"]

        print(f"[{idx:3}/120] {breed_name}")

        # --- breed info ---
        info = BREED_INFO.get(breed_name)
        if info:
            display_name, dog_type, hf, hm, life, temp, wf, wm = info
        else:
            print(f"         WARNING: missing info for {breed_name}")
            display_name = breed_name.replace("_", " ").replace("-", " ").title()
            dog_type, hf, hm, life, temp, wf, wm = "Unknown", 45, 50, "10-12", "Alert, Loyal", 15.0, 20.0

        # --- image ---
        image_url = None
        api_path = DOG_CEO_BREEDS.get(breed_name)
        if api_path:
            image_url = dog_ceo_image_url(api_path)

        if not image_url:
            wiki_title = WIKIPEDIA_TITLES.get(breed_name, display_name.replace(" ", "_"))
            print(f"         → Wikipedia fallback ({wiki_title})")
            image_url = wikipedia_image_url(wiki_title)

        img_path = os.path.join(IMAGES_DIR, f"{label}.jpg")
        if image_url:
            ok = download_image(image_url, img_path)
            if ok:
                size = os.path.getsize(img_path)
                print(f"         ✓ saved {size:,} bytes")
            else:
                print(f"         ✗ download failed")
                failed.append(breed_name)
        else:
            print(f"         ✗ no image URL found")
            failed.append(breed_name)

        dogs_json.append({
            "id": numeric_id,
            "index": idx,
            "name": display_name,
            "type": dog_type,
            "heightFemale": hf,
            "heightMale": hm,
            "lifeExpectancy": life,
            "temperament": temp,
            "weightFemale": wf,
            "weightMale": wm,
        })

        time.sleep(0.2)

    # --- save JSON ---
    with open(JSON_OUTPUT, "w", encoding="utf-8") as f:
        json.dump(dogs_json, f, indent=2, ensure_ascii=False)
    print(f"\ndogs.json → {JSON_OUTPUT}  ({len(dogs_json)} entries)")

    # --- zip images ---
    img_files = [f for f in os.listdir(IMAGES_DIR) if f.endswith(".jpg")]
    with zipfile.ZipFile(ZIP_OUTPUT, "w", zipfile.ZIP_DEFLATED) as zf:
        for fname in sorted(img_files):
            zf.write(os.path.join(IMAGES_DIR, fname), fname)
    zip_size = os.path.getsize(ZIP_OUTPUT) / 1024 / 1024
    print(f"dog_images.zip → {ZIP_OUTPUT}  ({len(img_files)} images, {zip_size:.1f} MB)")

    if failed:
        print(f"\nFailed ({len(failed)}): {', '.join(failed)}")
    else:
        print("\nAll images downloaded successfully.")


if __name__ == "__main__":
    main()
