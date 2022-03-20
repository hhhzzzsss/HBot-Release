package com.github.hhhzzzsss.hbot.processes;

import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.jse.JsePlatform;

// Currently unused and unfinished
public class LuaProcess extends CoreProcess {
    public static final int MAX_INSTRUCTIONS = 10000;
    LuaValue script;

    public LuaProcess(String script) {
        Globals globals = JsePlatform.standardGlobals();
        TimeoutDebugLib debugLib = new TimeoutDebugLib();
        globals.load(debugLib);
        debugLib.setInstructionTimeout(MAX_INSTRUCTIONS * 5);
        this.script = globals.load(script);
    }

    public class TimeoutDebugLib extends DebugLib {
        private int instructionTimeout = 0;

        public void setInstructionTimeout(int timeout) {
            instructionTimeout = timeout;
        }

        @Override
        public void onInstruction(int pc, Varargs v, int top) {
            if (instructionTimeout == 0) {
                throw new ScriptTimeoutException();
            }
            super.onInstruction(pc, v, top);
            instructionTimeout--;
        }

        public static class ScriptTimeoutException extends RuntimeException {}
    }

    @Override
    public void onSequence() {

    }
}
