package com.cometproject.api.modules;

import com.cometproject.api.modules.commands.CommandInfo;
import java.util.Map;

public record ModuleConfig(String name, String version, String entryPoint, Map<String, CommandInfo> commands) {

}
