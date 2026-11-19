"""Chaquopy bridge for gallery-dl.

The Android layer supplies a URL and an app-private workspace. gallery-dl writes
its extractor-specific files below that workspace; Java then copies the files to
the user's selected ClipNest destination.
"""

import json
import urllib.request
from importlib import metadata

from gallery_dl import config, job


def installed_version() -> str:
    try:
        return metadata.version("gallery-dl")
    except metadata.PackageNotFoundError:
        return "unknown"


def check_update() -> str:
    """Check PyPI without modifying the read-only bundled Python environment."""
    current = installed_version()
    request = urllib.request.Request(
        "https://pypi.org/pypi/gallery-dl/json",
        headers={"User-Agent": "ClipNest gallery-dl update checker"},
    )
    with urllib.request.urlopen(request, timeout=12) as response:
        latest = json.loads(response.read().decode("utf-8")).get("info", {}).get("version", "unknown")
    if current == latest:
        return f"Gallery-dl is up to date ({current})."
    return (
        f"Gallery-dl update available: {current} → {latest}. "
        "The bundled Android package is updated when ClipNest is rebuilt; this build will not overwrite its protected Python files."
    )


def download_gallery(url: str, output_dir: str) -> str:
    """Download one gallery URL and return a short completion message."""
    if not url or not url.strip():
        raise ValueError("Gallery URL is empty")
    if not output_dir or not output_dir.strip():
        raise ValueError("Gallery output directory is empty")

    config.load()
    config.set(("extractor",), "base-directory", output_dir)
    config.set(("output",), "mode", "terminal")
    try:
        result = job.DownloadJob(url.strip()).run()
    except Exception as exc:
        name = type(exc).__name__
        if name == "NoExtractorError":
            raise RuntimeError(
                "NoExtractorError: gallery-dl does not recognize this URL. "
                "Use a supported gallery/profile/image URL, not a search result or webpage wrapper."
            ) from exc
        raise RuntimeError(f"{name}: {exc}") from exc
    return "gallery-dl completed" if result is None else f"gallery-dl completed ({result})"
