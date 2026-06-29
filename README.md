# 🖊️ TutorialMaker

> A lightweight, offline-first Java desktop app for composing screenshot-based tutorials — drag images, add annotated labels and callouts, draw arrows and shapes, then export as PNG or print directly to PDF.

![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square&logo=openjdk)
![Swing](https://img.shields.io/badge/UI-Swing-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)
![Build](https://img.shields.io/badge/Build-NetBeans%20Ant-red?style=flat-square)

---

## ✨ What It Does

TutorialMaker gives you a **page canvas** (A4, A3, or Letter) where you can arrange tiles and annotations to build step-by-step tutorial layouts — no cloud, no subscription, no internet required.

| Feature | Details |
|---|---|
| 🖼️ **Image tiles** | Drop screenshots or photos; auto-scaled to fit |
| 📝 **Text tiles** | Rich text blocks with font, colour, and alignment control |
| 🏷️ **Label tiles** | Captions, speech-bubble callouts, and auto-numbered step badges |
| ✏️ **Draw layer** | Arrows, lines, rectangles, ovals, freehand strokes, and pinned text notes |
| 🔍 **Zoom** | 25 % – 300 %, Ctrl+scroll or toolbar buttons |
| ⊞ **Snap-to-grid** | 20 px grid for pixel-perfect alignment |
| ↩️ **Undo / Redo** | Full command history (Ctrl+Z / Ctrl+Y), 100-step depth |
| 💾 **Save / Load** | Custom `.tmk` project format (hand-rolled JSON, no external dependencies) |
| 📤 **Export** | PNG at native page resolution, or direct print via Java Print API |
| 📄 **Page sizes** | A4 Portrait/Landscape, A3 Portrait/Landscape, US Letter |

---

## 📸 Screenshot

```
┌─────────────────────────────────────────────────────────┐
│  FILE │ EDIT │ ADD │ CANVAS │ ZOOM │ PAGE │ EXPORT      │
│  ─────────────────────────────────────────────────────  │
│  DRAW LAYER  ✏ Draw Mode                                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│    ┌──────────────── white page ──────────────────┐    │
│    │  [Image Tile]   ① Step badge                 │    │
│    │  [Text Block ]  💬 Callout  ──arrow──▶        │    │
│    └──────────────────────────────────────────────┘    │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  Tiles: 4  |  100%  |  Image @ (120, 80)  240×160      │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- Java 8 or later (JRE is enough to run; JDK needed to build)
- NetBeans IDE (for the Ant-based project) **or** any IDE that can run Ant

### Build & Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/tutorial-maker.git
cd tutorial-maker

# Build with Ant (NetBeans project)
ant jar

# Run
java -jar dist/TutorialMaker.jar
```

Or open the project folder directly in **NetBeans** and press **Run (F6)**.

---

## 🎮 Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Ctrl+Z` | Undo |
| `Ctrl+Y` | Redo |
| `Ctrl+D` | Duplicate selected tile |
| `Ctrl+C / V / X` | Copy / Paste / Cut tile |
| `Arrow keys` | Nudge selected tile (1 px, or 20 px with grid on) |
| `Delete / Backspace` | Remove selected tile |
| `Escape` | Deselect tile |
| `Ctrl+Scroll` | Zoom in / out |

---

## 🗂️ Project Structure

```
src/tutorialmaker/
├── Main.java                    # Entry point
├── TutorialMakerFrame.java      # Top-level JFrame, layout & global shortcuts
│
├── canvas/
│   └── CanvasPanel.java         # Page canvas, zoom, grid, coordinate model
│
├── tiles/
│   ├── TileComponent.java       # Abstract base: drag, resize, handles, undo hooks
│   ├── ImageTile.java           # Screenshot / image tile
│   ├── TextTile.java            # Editable text block
│   └── LabelTile.java           # Caption, callout, and step badge
│
├── annotation/
│   ├── DrawingLayer.java        # Transparent overlay panel; manages shapes
│   ├── AnnotationTool.java      # Enum: SELECT, ARROW, LINE, RECT, OVAL, FREEHAND, TEXT_PIN, ERASER
│   ├── AnnotationShape.java     # Abstract base for all shapes
│   ├── ArrowShape.java
│   ├── LineShape.java
│   ├── RectShape.java
│   ├── OvalShape.java
│   ├── FreehandShape.java
│   └── TextPinShape.java
│
├── commands/                    # Command pattern — full undo/redo
│   ├── UndoableCommand.java
│   ├── UndoManager.java
│   ├── AddTileCommand.java
│   ├── DeleteTileCommand.java
│   ├── MoveTileCommand.java
│   └── ResizeTileCommand.java
│
├── io/
│   ├── ProjectSerializer.java   # Save/load .tmk projects (JSON)
│   ├── JsonWriter.java          # Hand-rolled JSON serialiser
│   └── JsonParser.java          # Hand-rolled JSON parser
│
├── properties/
│   ├── PropertiesPanel.java     # Right-side panel; swaps in type-specific subpanel
│   ├── CommonPropertiesPanel.java
│   ├── TextPropertiesPanel.java
│   ├── ImagePropertiesPanel.java
│   ├── LabelPropertiesPanel.java
│   └── PropUtil.java            # Shared widget factory helpers
│
└── toolbar/
    └── MainToolbar.java         # 4-row toolbar: File/Edit/Add, Canvas/Zoom/Export, Draw layer
```

---

## 💾 File Format (`.tmk`)

Projects are saved as plain UTF-8 JSON. Images are referenced by **absolute file path** — they are not embedded. If you move your screenshots, re-link them after opening.

```json
{
  "version": "1.0",
  "pageW": 794,
  "pageH": 1123,
  "zoom": 1.0,
  "snapToGrid": false,
  "showBorders": true,
  "tiles": [
    { "type": "image", "docX": 20, "docY": 20, "docW": 240, "docH": 160,
      "imagePath": "/home/user/screenshots/step1.png" },
    { "type": "label", "docX": 20, "docY": 200, "docW": 120, "docH": 40,
      "text": "Step 1", "style": "STEP_BADGE",
      "bgColor": "#2255cc", "textColor": "#ffffff" }
  ],
  "annotations": [
    { "type": "arrow", "color": "#dc3232", "thickness": 3.0,
      "fx1": 0.1, "fy1": 0.2, "fx2": 0.4, "fy2": 0.35 }
  ]
}
```

Annotation coordinates are **fractional** (0.0–1.0 relative to page size), so they survive page-size changes correctly.

---

## 🏗️ Architecture Notes

**Coordinate model** — tiles use *doc coords* (origin = top-left of the white page, zoom-independent). The canvas converts these to screen pixels on every repaint via:

```
screenX = PAGE_MARGIN × zoom + docX × zoom
```

This keeps the anchor point stable during zoom; the margin does not scale.

**Drawing layer** — a transparent `JPanel` overlaid on the page. It is completely hidden when draw mode is off, so mouse events reach tiles normally. Shapes store fractional coordinates so they are resolution- and zoom-independent.

**Undo system** — command pattern with two `ArrayDeque` stacks. Drag/resize commits an undo entry only on `mouseReleased`, not on every pixel, so history stays clean.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

---

## 📝 License

MIT — see [LICENSE](LICENSE) for details.
