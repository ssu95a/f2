package ru.inversion.f2.control;

public final class F2FlowEvent {

    private final F2FlowEventType type;

    public F2FlowEvent(F2FlowEventType type) {
        if (type == null)
            throw new IllegalArgumentException("type is null");

        this.type = type;
    }

    public F2FlowEventType type() {
        return type;
    }
}