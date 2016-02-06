package com.derpgroup.quip.logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.quip.QuipMetadata;

public class QuipLogger {
  private static final Logger LOG = LoggerFactory.getLogger(QuipLogger.class);
  
  /**
   * Primary metrics logging function. Logs a user request and associated metadata.
   * @param voiceInput
   */
  public static void log(VoiceInput voiceInput){
    String intent = voiceInput.getMessageSubject();
    if(intent.equals("ANOTHER")){return;}
    
    QuipMetadata metadata = (QuipMetadata)voiceInput.getMetadata();
    
    Deque<ConversationHistoryEntry> conversationHistory = metadata.getConversationHistory();
    int conversationHistorySize = 0;
    if(conversationHistory!=null){
      conversationHistorySize = conversationHistory.size();
    }

    LOG.info(
        getBotNumber(metadata.getBot()) +
        "," + intent +
        "," + metadata.getUserId() +
        "," + conversationHistorySize +
        getMessageMapValuesAsString(voiceInput.getMessageAsMap())   );
  }
  
  /**
   * Primary metrics logging function. Logs a user request and associated metadata.
   * Designed specifically for ANOTHER intents (which need to be unwrapped to reveal the intent they're repeating).
   * @param voiceInput
   */
  public static void logAnother(VoiceInput voiceInput){
    QuipMetadata metadata = (QuipMetadata)voiceInput.getMetadata();
    
    Deque<ConversationHistoryEntry> conversationHistory = metadata.getConversationHistory();
    int conversationHistorySize = 1;
    if(conversationHistory!=null){
      conversationHistorySize = conversationHistory.size()+1;
    }
    
    String intent = voiceInput.getMessageSubject();
    if(intent.equals("ANOTHER")){
      conversationHistorySize = 0;
      switch(metadata.getBot()){
      case "complibot":
        intent = "COMPLIMENT";
        break;
      case "insultibot":
        intent = "INSULT";
        break;
      default:
        intent = "UNKNOWN";
        break;
      }
    }

    LOG.info(
        getBotNumber(metadata.getBot()) +
        ",ANOTHER=" + intent +
        "," + metadata.getUserId() +
        "," + conversationHistorySize +
        getMessageMapValuesAsString(voiceInput.getMessageAsMap())   );
  }

  /**
   * A helper function for logging. Arranges key/value pairs in alphabetical order (by key)
   * and returns them in a comma delimited string:<br><br>
   * <i>,key1=value1,key2=value2  </i><br><br>
   * Please mind the leading comma. Each key=value pair is preceded by a comma for easy appending.
   * @param messageMap
   * @return
   */
  protected static String getMessageMapValuesAsString(Map<String, String> messageMap){
    List<String> orderedKeys = new ArrayList<String>();
    orderedKeys.addAll(messageMap.keySet());
    Collections.sort(orderedKeys);
    
    String orderedKeyValuePairs = "";
    for(String key: orderedKeys){
      orderedKeyValuePairs+=","+key+"="+messageMap.get(key);
    }
    return orderedKeyValuePairs;
  }

  /**
   * A helper function for logging. Each bot is represented by an integer to help with log compression.<br><br>
   * <ul>
   * <li>-1 = unrecognized</li>
   * <li>0 = complibot</li>
   * <li>1 = insultibot</li>
   * </ul>
   * @param botName
   * @return
   */
  protected static int getBotNumber(String botName){
    int botNumber = -1;
    switch(botName){
    case "complibot":
      botNumber = 0;
      break;
    case "insultibot":
      botNumber = 1;
      break;
    default:
      botNumber = -1;
      break;
    }
    return botNumber;
  }
}