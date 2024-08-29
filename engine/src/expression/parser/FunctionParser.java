package expression.parser;

import coordinate.Coordinate;
import expression.api.Expression;
import expression.impl.*;
import cell.api.CellType;
import cell.api.EffectiveValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static coordinate.CoordinateFactory.createCoordinate;

public enum FunctionParser {
    IDENTITY {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for IDENTITY function. Expected 1, but got " + arguments.size());
            }

            // all is good. create the relevant function instance
            String actualValue = arguments.get(0).trim();
            if (isBoolean(actualValue)) {
                return new IdentityExpression(Boolean.parseBoolean(actualValue), CellType.BOOLEAN);
            } else if (isNumeric(actualValue)) {
                return new IdentityExpression(Double.parseDouble(actualValue), CellType.NUMERIC);
            } else {
                return new IdentityExpression(actualValue, CellType.STRING);
            }
        }

        private boolean isBoolean(String value) {
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        }

        private boolean isNumeric(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    },
    PLUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function (e.g. number of arguments)
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PLUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for PLUS function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new PlusExpression(left, right);
        }
    },
    MINUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function (e.g. number of arguments)
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PLUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for MINUS function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new MinusExpression(left, right);
        }
    },
    TIMES {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for TIMES function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for TIMES function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new TimesExpression(left, right);
        }

    },

    DIVIDE {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for DIVIDE function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));
            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for DIVIDE function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new DivideExpression(left, right);
        }
    },

    MOD {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MOD function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for MOD function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new ModExpression(left, right);
        }
    },

    POW {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for POW function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));


            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for POW function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new PowExpression(left, right);
        }
    },

    ABS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for ABS function. Expected 1, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression exp = parseExpression(arguments.getFirst());

            // all is good. create the relevant function instance
            return new ABSExpression(exp);
        }
    },

    CONCAT {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for CONCAT function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

            // all is good. create the relevant function instance
            return new ConcatExpression(left, right);
        }
    },

    SUB {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for SUB function. Expected 3, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0));
            Expression middle = parseExpression(arguments.get(1));
            Expression right = parseExpression(arguments.get(2));

            // all is good. create the relevant function instance
            return new SubExpression(left, middle, right);
        }
    },

    REF {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for REF function. Expected 1, but got " + arguments.size());
            }

            // structure is good. parse arguments
            String cellId = arguments.getFirst().toUpperCase();

            Coordinate cellIdentifier =  createCoordinate(cellId);

            // create the relevant Ref function instance
            return new RefExpression(cellIdentifier);
        }
    };

    abstract public Expression parse(List<String> arguments);

    public static Expression parseExpression(String input) {

        if (input.startsWith("{") && input.endsWith("}")) {

            String functionContent = input.substring(1, input.length() - 1);
            List<String> topLevelParts = parseMainParts(functionContent);


            String functionName = topLevelParts.get(0).trim().toUpperCase();

            //remove the first element from the array
            topLevelParts.remove(0);
            return FunctionParser.valueOf(functionName).parse(topLevelParts);
        }

        // handle identity expression
        return FunctionParser.IDENTITY.parse(List.of(input.trim()));
    }

    private static List<String> parseMainParts(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : input.toCharArray()) {
            if (c == '{') {
                stack.push(c);
            } else if (c == '}') {
                stack.pop();
            }

            if (c == ',' && stack.isEmpty()) {
                // If we are at a comma and the stack is empty, it's a separator for top-level parts
                parts.add(buffer.toString().trim());
                buffer.setLength(0); // Clear the buffer for the next part
            } else {
                buffer.append(c);
            }
        }

        // Add the last part
        if (buffer.length() > 0) {
            parts.add(buffer.toString().trim());
        }

        return parts;
    }

}