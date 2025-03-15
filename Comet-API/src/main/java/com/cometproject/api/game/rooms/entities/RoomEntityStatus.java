package com.cometproject.api.game.rooms.entities;

public enum RoomEntityStatus {
    MOVE("mv", true),

    SIT("sit"),
    SIT_IN("sit-in", true),
    SIT_OUT("sit-out"),

    LAY_IN("lay-in"),
    LAY("lay", true),
    LAY_OUT("lay-out"),

    SIGN("sign"),
    CONTROLLER("flatctrl"),
    GESTURE("gst"),
    WAVE("wav"),
    TRADE("trd"),
    
    VOTE("vote"),

    PLAY_DEAD_IN("ded-in"),
    PLAY_DEAD("ded", true),
    PLAY_DEAD_OUT("ded-out"),

    PLAY_IN("pla-in"),
    PLAY("pla", true),
    PLAY_OUT("pla-out"),

    JUMP_IN("jmp-in"),
    JUMP("jmp", true),
    JUMP_OUT("jmp-out"),

    EAT_IN("eat-in"),
    EAT("eat"),
    EAT_OUT("eat-out"),

    BEG("beg", true),

    SPEAK("spk"),
    CROAK("crk"),
    RELAX("rlx"),
    WINGS("wng", true),
    FLAME("flm"),
    RIP("rip"),
    GROW("grw"),
    GROW_1("grw1"),
    GROW_2("grw2"),
    GROW_3("grw3"),
    GROW_4("grw4"),
    GROW_5("grw5"),
    GROW_6("grw6"),
    GROW_7("grw7"),

    KICK("kck"),
    WAG_TAIL("wag"),
    DANCE("dan"),
    AMS("ams"),
    SWIM("swm"),
    TURN("trn"),

    SRP("srp"),
    SRP_IN("srp-in"),

    SLEEP_IN("slp-in"),
    SLEEP("slp", true),
    SLEEP_OUT("slp-out"),

    DIP("dip");

    private final String statusCode;
    private final boolean removeWhenWalking;

    RoomEntityStatus(String statusCode, boolean removeWhenWalking) {
        this.statusCode = statusCode;
        this.removeWhenWalking = removeWhenWalking;
    }

    RoomEntityStatus(String statusCode) {
        this(statusCode, false);
    }

    @Override
    public String toString() {
        return this.statusCode;
    }

    public static RoomEntityStatus fromString(String key) {
        return java.util.Arrays.stream(values())
            .filter(status -> status.statusCode.equalsIgnoreCase(key))
            .findFirst()
            .orElse(null);
    }

}
