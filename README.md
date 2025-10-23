# VTaskMaster
**VTaskMaster** — a sleek Java Swing To-Do app with task management, live clock, dark/light themes, auto-save, and compact mode. Stay organized effortlessly with a clean, modern interface.

VTaskMaster is a compact, professional-looking **Java Swing To-Do app** suitable for beginners.  

Features:
- Add / Edit / Delete tasks
- Mark tasks as done (toggle)
- Priority (Low / Medium / High) and optional due text
- Save tasks to `data/tasks.txt` automatically on exit and manually via **Save Now**
- Dark and Light theme toggle
- Compact view checkbox
- Live clock in the header
- Double-click list item to quickly edit title
- Proper project structure for GitHub

## Files
- `src/Task.java` - Task model and CSV serialization
- `src/VTaskMaster.java` - Main Swing application
- `data/tasks.txt` - Stored tasks (created automatically)

## How to run
1. Make sure you have **Java 8+** installed.
2. Compile:
```bash
javac -d bin -sourcepath src src/*.java
```
3. Run:
```bash
java -cp bin VTaskMaster
```
Alternatively, open the project in an IDE like IntelliJ IDEA or Eclipse:
- Create a new Java project
- Add `src` as source folder
- Run `VTaskMaster.java`

## Notes & Improvements
- The app stores tasks as a simple `|`-separated CSV in `data/tasks.txt`. It's intentionally simple for learning.
- You can improve by adding proper date parsing, notifications, or exporting/importing JSON.
- Pull requests welcome! 
