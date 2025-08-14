# CalcPro — Custom Formula Interpreter 📐e = ∑∞ⁿ⁼⁰ ¹ₙ🤓

<p align="center"><img width="800" alt="image" src="https://github.com/user-attachments/assets/203527cb-a4a9-4e14-895b-43b1bdce3f12" /></p> 


> **CalcPro** is a colourful, math-inclined single-file Java demo that implements the **Interpreter Design Pattern** to parse and evaluate custom formulas like `ADD(5, 10)`, `MULTIPLY(ADD(2,3),4)` and `DIVIDE(SUBTRACT(20,4),2)`. It also supports variables via a `Context` (e.g., `x = 5`).

---

## 🚀 Highlights

* Clean **AST-based interpreter**: each operation is an `Expression` implementation (Add, Subtract, Multiply, Divide, …).
* **Operator registry** — register new operators (e.g., `POWER`) without touching parser code.
* Interactive **Swing GUI**: colourful header, math doodles, variables panel and instant evaluation.
* Simple, single-file implementation ideal for learning, prototyping, or extending.

---

## 📁 Repository

**GitHub:** `https://github.com/Tharindu714/CalcPro-Custom-Formula-Interpreter`

---

## 🧪 Quick demo (console outputs)

* `ADD(5, 10)` → `15.0`
* `MULTIPLY(ADD(2, 3), 4)` → `20.0`
* `DIVIDE(SUBTRACT(20, 4), 2)` → `8.0`
* `MULTIPLY(ADD(x, 3), 2)` with `x = 5` → `16.0`

---

## 🛠️ Build & Run

1. Ensure you have Java (JDK 8+) installed.
2. Clone the repo:

```bash
git clone https://github.com/Tharindu714/CalcPro-Custom-Formula-Interpreter.git
cd CalcPro-Custom-Formula-Interpreter
```

3. Compile & run the single Java file (it's included at the root):

```bash
javac CalcPro_Interpreter_GUI.java
java CalcPro_Interpreter_GUI
```

> The app prints demo outputs to the console and opens the colourful CalcPro GUI.

---

## 🧠 Design overview (Interpreter pattern)

* **Expression (interface)** — exposes `double interpret(Context ctx)`.
* **Terminal expressions** — `NumberExpression`, `VariableExpression`.
* **Non-terminal (composite/binary)** — `BinaryExpression` (left/right) with subclasses `AddExpression`, `SubtractExpression`, `MultiplyExpression`, `DivideExpression`, `PowerExpression`.
* **Context** — holds variable name/value map and resolves variables at runtime.
* **FormulaParser** — parses strings into an AST, handles nested function calls and top-level comma splitting.
* **OperatorRegistry** — maps operation names (`ADD`, `POWER`) to small factory lambdas that build `Expression` objects.

This separation keeps the parser stable while new functionality is added by registering new expression creators.

---

## ✨ Adding a new operator (example: `POWER`)

1. Create the expression (already present in code as `PowerExpression`):

```java
class PowerExpression extends BinaryExpression {
    public PowerExpression(Expression left, Expression right) { super(left, right); }
    @Override
    public double interpret(Context ctx) throws Exception {
        return Math.pow(left.interpret(ctx), right.interpret(ctx));
    }
}
```

2. Register it in `OperatorRegistry` (one line):

```java
OperatorRegistry.register("POWER", args -> {
    requireArgCount("POWER", args, 2);
    return new PowerExpression(args.get(0), args.get(1));
});
```

No parser changes required — just class + registry entry.

---

## 📐 UML (PlantUML) — Class Diagram & Sequence Diagram

### Class Diagram (PlantUML)

<img width="1347" height="441" alt="image" src="https://github.com/user-attachments/assets/33089698-9a89-4ee0-bdc2-99c538f41b02" />

---

### Sequence Diagram (PlantUML)

<img width="1041" height="543" alt="image" src="https://github.com/user-attachments/assets/fcc45adf-9a67-4b5d-a424-2ff0f8b3a261" />

---

## 🖼️ Scenario

<p align="center">
  <img src="https://github.com/user-attachments/assets/8f6d0ca7-49c5-46fc-b253-009bedd558ec" 
       alt="WhatsApp Image 2025-08-14 at 14 44 55" width="750">
</p>

---

## ✅ Contribution

Contributions welcome — fork the repo, add operators, file issues, or submit PRs. If you add interesting operators (TRIG, LOG, SUM over lists), please include tests and example formulas.

---

## 📜 License

MIT — feel free to reuse and adapt.

---

*Created with ❤️ for learners and tinkers — Tharindu's CalcPro demo.*


