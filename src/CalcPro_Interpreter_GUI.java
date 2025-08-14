import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/*
 * CalcPro Interpreter + GUI
 * Single-file Java implementation demonstrating the Interpreter design pattern.
 * - Parses formulas like: ADD(5, 10), MULTIPLY(ADD(2,3), 4), DIVIDE(SUBTRACT(20,4), 2)
 * - Supports variables via Context (e.g., x = 5)
 * - Each operation is its own Expression class (AddExpression, SubtractExpression, ...)
 * - Parser uses an operator registry so new operations require minimal changes
 *
 * To compile & run:
 * javac CalcPro_Interpreter_GUI.java
 * java CalcPro_Interpreter_GUI
 */

public class CalcPro_Interpreter_GUI {
    public static void main(String[] args) {
        // Console demo (client usage examples)
        Context demoCtx = new Context();
        demoCtx.setVariable("x", 5);

        try {
            Expression e1 = FormulaParser.parse("ADD(5, 10)");
            System.out.println("ADD(5, 10) = " + e1.interpret(demoCtx)); // 15

            Expression e2 = FormulaParser.parse("MULTIPLY(ADD(2, 3), 4)");
            System.out.println("MULTIPLY(ADD(2, 3), 4) = " + e2.interpret(demoCtx)); // 20

            Expression e3 = FormulaParser.parse("DIVIDE(SUBTRACT(20, 4), 2)");
            System.out.println("DIVIDE(SUBTRACT(20, 4), 2) = " + e3.interpret(demoCtx)); // 8

            Expression e4 = FormulaParser.parse("MULTIPLY(ADD(x, 3), 2)");
            System.out.println("MULTIPLY(ADD(x, 3), 2) with x=5 -> " + e4.interpret(demoCtx)); // 16

        } catch (Exception ex) {
            System.err.println("Demo error: " + ex.getMessage());
        }

        // Launch GUI
        SwingUtilities.invokeLater(() -> new CalcProFrame().setVisible(true));
    }
}

/* ---------------------- Interpreter core ---------------------- */
interface Expression {
    double interpret(Context ctx) throws Exception;
}

class Context {
    private final Map<String, Double> variables = new HashMap<>();

    public void setVariable(String name, double value) {
        variables.put(name.toUpperCase(Locale.ROOT), value);
    }

    public Optional<Double> getVariable(String name) {
        return Optional.ofNullable(variables.get(name.toUpperCase(Locale.ROOT)));
    }

    public Map<String, Double> getAllVariables() {
        return Collections.unmodifiableMap(variables);
    }
}

/* Concrete Expressions */
class NumberExpression implements Expression {
    private final double value;

    public NumberExpression(double value) {
        this.value = value;
    }

    @Override
    public double interpret(Context ctx) {
        return value;

    }
}

class VariableExpression implements Expression {
    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    @Override
    public double interpret(Context ctx) throws Exception {
        return ctx.getVariable(name)
                .orElseThrow(() -> new Exception("Undefined variable: " + name));

    }
}

abstract class BinaryExpression implements Expression {
    protected final Expression left, right;

    public BinaryExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
}

class AddExpression extends BinaryExpression {
    public AddExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double interpret(Context ctx) throws Exception {
        return left.interpret(ctx) + right.interpret(ctx);
    }
}

class SubtractExpression extends BinaryExpression {
    public SubtractExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double interpret(Context ctx) throws Exception {
        return left.interpret(ctx) - right.interpret(ctx);
    }
}

class MultiplyExpression extends BinaryExpression {
    public MultiplyExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double interpret(Context ctx) throws Exception {
        return left.interpret(ctx) * right.interpret(ctx);
    }
}

class DivideExpression extends BinaryExpression {
    public DivideExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double interpret(Context ctx) throws Exception {
        double divideNumber = right.interpret(ctx);
        if (divideNumber == 0) throw new Exception("Division by zero");
        return left.interpret(ctx) / divideNumber;
    }
}

// Example of how POWER would look. (Not registered by default here — you can register it.)
class PowerExpression extends BinaryExpression {
    public PowerExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double interpret(Context ctx) throws Exception {
        return Math.pow(left.interpret(ctx), right.interpret(ctx));
    }
}

/* ---------------------- Parser & Operator Registry ---------------------- */
interface ExpressionCreator {
    Expression create(List<Expression> args) throws Exception;
}

class OperatorRegistry {
    private static final Map<String, ExpressionCreator> registry = new HashMap<>();

    static {
        // Register core operators
        register("ADD", args -> {
            requireArgCount("ADD", args);
            return new AddExpression(args.get(0), args.get(1));
        });

        register("SUBTRACT", args -> {
            requireArgCount("SUBTRACT", args);
            return new SubtractExpression(args.get(0), args.get(1));
        });

        register("MULTIPLY", args -> {
            requireArgCount("MULTIPLY", args);
            return new MultiplyExpression(args.get(0), args.get(1));
        });

        register("DIVIDE", args -> {
            requireArgCount("DIVIDE", args);
            return new DivideExpression(args.get(0), args.get(1));
        });

        OperatorRegistry.register("POWER", args -> {
            if (args.size() != 2) throw new Exception("POWER requires 2 arguments");
            return new PowerExpression(args.get(0), args.get(1));
        });

        // Note: POWER is not registered by default here. See documentation below for adding it.
    }

    public static void register(String name, ExpressionCreator creator) {
        registry.put(name.toUpperCase(Locale.ROOT), creator);
    }

    public static Optional<ExpressionCreator> get(String name) {
        return Optional.ofNullable(registry.get(name.toUpperCase(Locale.ROOT)));
    }

    private static void requireArgCount(String op, List<Expression> args) throws Exception {
        if (args.size() != 2) throw new Exception(op + " requires " + 2 + " arguments");
    }
}

class FormulaParser {
    public static Expression parse(String input) throws Exception {
        if (input == null) throw new Exception("Empty expression");
        String s = input.trim();
        return parseExpression(s);
    }

    private static Expression parseExpression(String s) throws Exception {
        s = s.trim();
        // Number?
        if (s.matches("[+-]?(\\d+\\.\\d*|\\.\\d+|\\d+)")) {
            return new NumberExpression(Double.parseDouble(s));
        }
        // Variable?
        if (s.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return new VariableExpression(s);
        }
        // Function call like NAME(arg1, arg2)
        int firstParen = s.indexOf('(');
        int lastParen = s.lastIndexOf(')');
        if (firstParen > 0 && lastParen == s.length() - 1) {
            String name = s.substring(0, firstParen).trim();
            String inside = s.substring(firstParen + 1, lastParen).trim();
            List<String> argStrings = splitArguments(inside);
            List<Expression> args = new ArrayList<>();
            for (String as : argStrings) {
                args.add(parseExpression(as));
            }
            // Lookup operator
            Optional<ExpressionCreator> oc = OperatorRegistry.get(name);
            if (oc.isPresent()) {
                return oc.get().create(args);
            } else {
                throw new Exception("Unknown operator/function: " + name);
            }
        }
        throw new Exception("Cannot parse expression: " + s);
    }

    // Splits top-level comma-separated arguments, respecting nested parentheses
    private static List<String> splitArguments(String s) {
        List<String> out = new ArrayList<>();
        if (s.isEmpty()) return out;
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' && depth == 0) {
                out.add(cur.toString().trim());
                cur = new StringBuilder();
                continue;
            }
            if (c == '(') depth++;
            if (c == ')') depth--;
            cur.append(c);
        }
        out.add(cur.toString().trim());
        return out;
    }
}

/* ---------------------- Colorful Math-inclined GUI ---------------------- */
class CalcProFrame extends JFrame {

    private final JTextField formulaField = new JTextField();
    private final JLabel resultLabel = new JLabel(" ");
    private final DefaultListModel<String> varListModel = new DefaultListModel<>();
    private final Map<String, Double> variableMap = new HashMap<>();

    public CalcProFrame() {
        setTitle("CalcPro — Formula Interpreter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(new HeaderPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Left: main calculator panel
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createTitledBorder("Formula"));

        formulaField.setFont(new Font("Poppins", Font.PLAIN, 16));
        formulaField.setText("ADD(5, 10)");
        left.add(formulaField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton evalBtn = new JButton("Evaluate");
        JButton examplesBtn = new JButton("Load Examples");
        buttons.add(evalBtn);
        buttons.add(examplesBtn);
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(buttons);

        resultLabel.setFont(new Font("Inter", Font.BOLD, 22));
        resultLabel.setOpaque(true);
        resultLabel.setBackground(new Color(255, 255, 255, 200));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(new JLabel("Result:"));
        left.add(resultLabel);

        // Right: variables panel
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createTitledBorder("Variables (case-insensitive)"));

        JPanel addVar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField varName = new JTextField(6);
        JTextField varValue = new JTextField(6);
        JButton addBtn = new JButton("Add / Update");
        addVar.add(new JLabel("Name:"));
        addVar.add(varName);
        addVar.add(new JLabel("Value:"));
        addVar.add(varValue);
        addVar.add(addBtn);
        right.add(addVar);

        JList<String> varList = new JList<>(varListModel);
        varList.setVisibleRowCount(8);
        JScrollPane sp = new JScrollPane(varList);
        right.add(sp);

        JButton clearBtn = new JButton("Clear Variables");
        right.add(clearBtn);

        JPanel main = new JPanel(new GridLayout(1, 2, 12, 12));
        main.add(left);
        main.add(right);

        center.add(main, BorderLayout.CENTER);

        // Footer: helpful tips
        JTextArea tips = new JTextArea();
        tips.setText("Tips:\n- Use functions like ADD(a, b), SUBTRACT(a, b), MULTIPLY(a, b), DIVIDE(a, b)\n- Nested calls supported: MULTIPLY(ADD(2,3),4)\n- Add variables (e.g., x = 5) and use them in formulas: MULTIPLY(ADD(x, 3), 2)");
        tips.setEditable(false);
        tips.setBackground(new Color(0, 0, 0, 0));
        center.add(tips, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // Action handlers
        evalBtn.addActionListener(e -> evaluateFormula(varListModel));
        examplesBtn.addActionListener(e -> loadExamples());

        addBtn.addActionListener(e -> {
            String name = varName.getText().trim();
            String val = varValue.getText().trim();
            if (name.isEmpty() || val.isEmpty()) return;
            try {
                double v = Double.parseDouble(val);
                variableMap.put(name.toUpperCase(Locale.ROOT), v);
                varListModel.addElement(name + " = " + v);
                varName.setText("");
                varValue.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number: " + val);
            }
        });

        clearBtn.addActionListener(e -> {
            variableMap.clear();
            varListModel.clear();
        });

        // Evaluate on pressing ENTER in formula field
        formulaField.addActionListener(e -> evaluateFormula(varListModel));
    }

    private void evaluateFormula(DefaultListModel<String> ignoredVarListModel) {
        String f = formulaField.getText();
        Context ctx = new Context();
        variableMap.forEach(ctx::setVariable);
        try {
            Expression exp = FormulaParser.parse(f);
            double res = exp.interpret(ctx);
            resultLabel.setText(String.valueOf(res));
            ctx.getAllVariables().forEach((k, v) -> System.out.println(k + " = " + v));
            // nice green flash
            resultLabel.setBackground(new Color(200, 255, 200));
        } catch (Exception e) {
            resultLabel.setText("Error: " + e.getMessage());
            resultLabel.setBackground(new Color(255, 200, 200));
        }
    }

    private void loadExamples() {
        String[] examples = new String[]{
                "ADD(5, 10)",
                "MULTIPLY(ADD(2, 3), 4)",
                "DIVIDE(SUBTRACT(20, 4), 2)",
                "MULTIPLY(ADD(x, 3), 2)"
        };
        String sel = (String) JOptionPane.showInputDialog(this, "Choose example:", "Examples",
                JOptionPane.PLAIN_MESSAGE, null, examples, examples[0]);
        if (sel != null) formulaField.setText(sel);
    }
}

// Header with gradient and math doodles
class HeaderPanel extends JPanel {
    public HeaderPanel() {
        setPreferredSize(new Dimension(100, 120));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 87, 153), w, h, new Color(125, 185, 232));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 180));
        g2.setFont(new Font("Poppins", Font.BOLD, 28));
        g2.drawString("CalcPro — Custom Formula Interpreter", 18, 40);

        g2.setFont(new Font("Inter", Font.PLAIN, 14));
        g2.drawString("Enter formulas like ADD(5,10) or MULTIPLY(ADD(2,3),4). Supports variables.", 18, 64);

        // Draw math doodles
        g2.setStroke(new BasicStroke(2f));
        g2.setFont(new Font("Serif", Font.BOLD, 36));
        g2.drawString("Σ", w - 120, 60);
        g2.drawString("π", w - 80, 90);
        g2.drawOval(w - 200, 20, 50, 50);
    }
}