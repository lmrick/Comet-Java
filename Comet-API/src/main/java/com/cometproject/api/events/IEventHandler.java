package com.cometproject.api.events;

import com.cometproject.api.modules.commands.CommandInfo;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.utilities.Initializable;
import java.util.Map;
import java.util.function.BiConsumer;

public interface IEventHandler extends Initializable {
   
   <T extends EventArgs> boolean handleEvent(Class<? extends Event<T>> eventClass, T args);
   void registerEvent(Event<?> consumer);

   void registerChatCommand(String commandExecutor, BiConsumer<ISession, String[]> consumer);
   void registerCommandInfo(String commandName, CommandInfo info);
   Map<String, CommandInfo> getCommands();
   boolean handleCommand(ISession session, String commandExecutor, String[] arguments);
   
}
