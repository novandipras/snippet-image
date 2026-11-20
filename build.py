"""Build offline against an installed Android Studio SDK, without downloading dependencies."""
import os
from pathlib import Path
import subprocess
import zipfile

root = Path(__file__).resolve().parent
default_sdk = (Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Android/Android Studio"
               if os.name == "nt" else Path("/Applications/Android Studio.app/Contents"))
sdk = Path(os.environ.get("ANDROID_STUDIO_HOME", str(default_sdk))).expanduser()
java_home = sdk / "jbr/Contents/Home"
if not java_home.is_dir():
    java_home = sdk / "jbr"
javac = java_home / "bin" / ("javac.exe" if os.name == "nt" else "javac")
if not javac.is_file():
    raise SystemExit("Set ANDROID_STUDIO_HOME to an Android Studio installation containing JBR/JDK.")
classes = root / "build/snippet-image/classes"
classes.mkdir(parents=True, exist_ok=True)
sources = sorted((root / "src/main/java").rglob("*.java"))
# Java expands the wildcard; keep Windows command lines below the process length limit.
classpath = str(sdk / "lib" / "*")
subprocess.run([str(javac), "--release", "21", "-proc:none", "-encoding", "UTF-8", "-classpath", classpath,
                "-d", str(classes), *map(str, sources)], check=True)
jar_path = root / "build/snippet-image.jar"
with zipfile.ZipFile(jar_path, "w", zipfile.ZIP_DEFLATED) as jar:
    for path in sorted(classes.rglob("*.class")):
        jar.write(path, path.relative_to(classes).as_posix())
    jar.write(root / "src/main/resources/META-INF/plugin.xml", "META-INF/plugin.xml")
dist = root / "dist"
dist.mkdir(exist_ok=True)
archive = dist / "snippet-image-0.1.1.zip"
with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as package:
    package.write(jar_path, "snippet-image/lib/snippet-image.jar")
print(f"Built {archive} ({archive.stat().st_size:,} bytes)")
