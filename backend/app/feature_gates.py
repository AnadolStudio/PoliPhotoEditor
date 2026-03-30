"""
Feature gate definitions for PoliPhotoEditor.

Two access levels:
  - guest   : unauthenticated user (no JWT)
  - person  : any registered/authenticated user

`guest_allowed=True`  → feature is available without login
`guest_allowed=False` → feature requires authentication (person role)
"""

from typing import Optional

FEATURES: dict[str, dict] = {
    "filters_basic":        {"guest_allowed": True,  "description": "Basic filters (first 20)"},
    "filters_all":          {"guest_allowed": False, "description": "All filters (70+)"},
    "crop_rotate_flip":     {"guest_allowed": True,  "description": "Crop, rotate, flip"},
    "improve_basic":        {"guest_allowed": True,  "description": "Brightness, contrast, saturation"},
    "curves":               {"guest_allowed": False, "description": "Professional curves adjustment"},
    "effects":              {"guest_allowed": False, "description": "Overlay effects"},
    "text_basic":           {"guest_allowed": True,  "description": "Add text"},
    "fonts_extended":       {"guest_allowed": False, "description": "Extended font library"},
    "stickers_basic":       {"guest_allowed": True,  "description": "Basic stickers (3 categories)"},
    "stickers_all":         {"guest_allowed": False, "description": "All stickers (11 categories)"},
    "frames_basic":         {"guest_allowed": True,  "description": "Basic frames (5)"},
    "frames_all":           {"guest_allowed": False, "description": "All frames (29+)"},
    "collage_basic":        {"guest_allowed": True,  "description": "Basic collages (3 templates)"},
    "collage_all":          {"guest_allowed": False, "description": "All collages (29 templates)"},
    "forms_basic":          {"guest_allowed": True,  "description": "Basic forms (3 templates)"},
    "forms_all":            {"guest_allowed": False, "description": "All forms (16+ templates)"},
    "background_removal":   {"guest_allowed": False, "description": "ML background removal"},
    "body_editor":          {"guest_allowed": False, "description": "Body shape editor"},
    "export_no_watermark":  {"guest_allowed": False, "description": "Export without watermark"},
}

# Filters: id 0-19 are available to guests, 20+ require person
FILTERS: list[dict] = [
    {"id": 0,  "name": "Original", "group": "Original"},
    {"id": 1,  "name": "SD1",  "group": "Simple Dark"},
    {"id": 2,  "name": "SD2",  "group": "Simple Dark"},
    {"id": 3,  "name": "SD3",  "group": "Simple Dark"},
    {"id": 4,  "name": "SD4",  "group": "Simple Dark"},
    {"id": 5,  "name": "SD5",  "group": "Simple Dark"},
    {"id": 6,  "name": "SP1",  "group": "Sepia"},
    {"id": 7,  "name": "SP2",  "group": "Sepia"},
    {"id": 8,  "name": "SP3",  "group": "Sepia"},
    {"id": 9,  "name": "SP4",  "group": "Sepia"},
    {"id": 10, "name": "SP5",  "group": "Sepia"},
    {"id": 11, "name": "SP6",  "group": "Sepia"},
    {"id": 12, "name": "SP7",  "group": "Sepia"},
    {"id": 13, "name": "SP8",  "group": "Sepia"},
    {"id": 14, "name": "SP9",  "group": "Sepia"},
    {"id": 15, "name": "SP10", "group": "Sepia"},
    {"id": 16, "name": "SP11", "group": "Sepia"},
    {"id": 17, "name": "SP12", "group": "Sepia"},
    {"id": 18, "name": "LG1",  "group": "Light"},
    {"id": 19, "name": "LG2",  "group": "Light"},
    {"id": 20, "name": "LG3",  "group": "Light"},
    {"id": 21, "name": "LG4",  "group": "Light"},
    {"id": 22, "name": "LG5",  "group": "Light"},
    {"id": 23, "name": "LG6",  "group": "Light"},
    {"id": 24, "name": "BL1",  "group": "Blur"},
    {"id": 25, "name": "BL2",  "group": "Blur"},
    {"id": 26, "name": "SH1",  "group": "Sharp"},
    {"id": 27, "name": "SH2",  "group": "Sharp"},
    {"id": 28, "name": "SH3",  "group": "Sharp"},
    {"id": 29, "name": "SH4",  "group": "Sharp"},
    {"id": 30, "name": "NT1",  "group": "Negative"},
    {"id": 31, "name": "NT2",  "group": "Negative"},
    {"id": 32, "name": "NT3",  "group": "Negative"},
    {"id": 33, "name": "NT4",  "group": "Negative"},
    {"id": 34, "name": "NT5",  "group": "Negative"},
    {"id": 35, "name": "NT6",  "group": "Negative"},
    {"id": 36, "name": "MZ1",  "group": "Mozaic"},
    {"id": 37, "name": "MZ2",  "group": "Mozaic"},
    {"id": 38, "name": "MZ3",  "group": "Mozaic"},
    {"id": 39, "name": "DT1",  "group": "Desert"},
    {"id": 40, "name": "DT2",  "group": "Desert"},
    {"id": 41, "name": "DT3",  "group": "Desert"},
    {"id": 42, "name": "DT4",  "group": "Desert"},
    {"id": 43, "name": "DT5",  "group": "Desert"},
    {"id": 44, "name": "DT6",  "group": "Desert"},
    {"id": 45, "name": "DT7",  "group": "Desert"},
    {"id": 46, "name": "DT8",  "group": "Desert"},
    {"id": 47, "name": "DT9",  "group": "Desert"},
    {"id": 48, "name": "WV1",  "group": "Waves"},
    {"id": 49, "name": "WV2",  "group": "Waves"},
    {"id": 50, "name": "SL1",  "group": "Soft Light"},
    {"id": 51, "name": "SL2",  "group": "Soft Light"},
    {"id": 52, "name": "SL3",  "group": "Soft Light"},
    {"id": 53, "name": "SL4",  "group": "Soft Light"},
    {"id": 54, "name": "BW1",  "group": "Black & White"},
    {"id": 55, "name": "BW2",  "group": "Black & White"},
    {"id": 56, "name": "CL1",  "group": "Color"},
    {"id": 57, "name": "CL2",  "group": "Color"},
    {"id": 58, "name": "CL3",  "group": "Color"},
    {"id": 59, "name": "CL4",  "group": "Color"},
    {"id": 60, "name": "CL5",  "group": "Color"},
    {"id": 61, "name": "CL6",  "group": "Color"},
    {"id": 62, "name": "RB1",  "group": "Rainbow"},
    {"id": 63, "name": "RB2",  "group": "Rainbow"},
    {"id": 64, "name": "RB3",  "group": "Rainbow"},
    {"id": 65, "name": "RB4",  "group": "Rainbow"},
    {"id": 66, "name": "GR1",  "group": "Green"},
    {"id": 67, "name": "GR2",  "group": "Green"},
    {"id": 68, "name": "GR3",  "group": "Green"},
    {"id": 69, "name": "GR4",  "group": "Green"},
    {"id": 70, "name": "GR5",  "group": "Green"},
    {"id": 71, "name": "GR6",  "group": "Green"},
    {"id": 72, "name": "GR7",  "group": "Green"},
    {"id": 73, "name": "GR8",  "group": "Green"},
    {"id": 74, "name": "OC1",  "group": "Ocean"},
    {"id": 75, "name": "OC2",  "group": "Ocean"},
    {"id": 76, "name": "OC3",  "group": "Ocean"},
    {"id": 77, "name": "WM1",  "group": "Warmth"},
    {"id": 78, "name": "WM2",  "group": "Warmth"},
    {"id": 79, "name": "WM3",  "group": "Warmth"},
    {"id": 80, "name": "WM4",  "group": "Warmth"},
    {"id": 81, "name": "WM5",  "group": "Warmth"},
    {"id": 82, "name": "WM6",  "group": "Warmth"},
    {"id": 83, "name": "BB1",  "group": "Bright Blue"},
    {"id": 84, "name": "BB2",  "group": "Bright Blue"},
    {"id": 85, "name": "BB3",  "group": "Bright Blue"},
    {"id": 86, "name": "PK1",  "group": "Pink"},
    {"id": 87, "name": "PK2",  "group": "Pink"},
    {"id": 88, "name": "PK3",  "group": "Pink"},
    {"id": 89, "name": "PK4",  "group": "Pink"},
    {"id": 90, "name": "PK5",  "group": "Pink"},
    {"id": 91, "name": "PK6",  "group": "Pink"},
    {"id": 92, "name": "PK7",  "group": "Pink"},
    {"id": 93, "name": "PK8",  "group": "Pink"},
    {"id": 94, "name": "PK9",  "group": "Pink"},
]

GUEST_FILTER_LIMIT = 20  # filters with id < 20 are guest-accessible


def get_feature_access(is_authenticated: bool) -> dict[str, bool]:
    """Return {feature: available} map for given auth state."""
    return {
        key: True if cfg["guest_allowed"] else is_authenticated
        for key, cfg in FEATURES.items()
    }


def get_filters_with_access(is_authenticated: bool) -> list[dict]:
    """Return full filter list with `available` flag."""
    return [
        {**f, "available": is_authenticated or f["id"] < GUEST_FILTER_LIMIT}
        for f in FILTERS
    ]
