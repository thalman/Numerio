package net.halman.numerio;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Parenthesis implements Serializable {
    public enum Type {
        OPENING,
        CLOSING
    }

    public Type type;

    public Parenthesis(Type type)
    {
        this.type = type;
    }

    public Parenthesis(@NotNull Parenthesis other)
    {
        this.type = other.type;
    }

    public Parenthesis(char c)
    {
        this.type = Type.CLOSING;
        if (c == '(' || c == '[') {
            this.type = Type.OPENING;
        }
    }

    public boolean closing()
    {
        return type == Type.CLOSING;
    }

    public boolean opening()
    {
        return type == Type.OPENING;
    }

    @Override
    public String toString()
    {
        if (type == Type.CLOSING) {
            return ")";
        }
        if (type == Type.OPENING) {
            return "(";
        }
        return "?";
    }
}
