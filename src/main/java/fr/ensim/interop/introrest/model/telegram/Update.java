package fr.ensim.interop.introrest.model.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @author Ruben Bermudez
 * @version 1.0
 *
 * This object represents an incoming update.
 *
 * @apiNote Only one of the optional parameters can be present in any given update.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Update implements BotApiObject {
    private static final String UPDATEID_FIELD = "update_id";
    private static final String MESSAGE_FIELD = "message";
    private static final String INLINEQUERY_FIELD = "inline_query";
    private static final String CHOSENINLINEQUERY_FIELD = "chosen_inline_result";
    private static final String CALLBACKQUERY_FIELD = "callback_query";
    private static final String EDITEDMESSAGE_FIELD = "edited_message";
    private static final String CHANNELPOST_FIELD = "channel_post";
    private static final String EDITEDCHANNELPOST_FIELD = "edited_channel_post";
    private static final String SHIPPING_QUERY_FIELD = "shipping_query";
    private static final String PRE_CHECKOUT_QUERY_FIELD = "pre_checkout_query";
    private static final String POLL_FIELD = "poll";
    private static final String POLLANSWER_FIELD = "poll_answer";
    private static final String MYCHATMEMBER_FIELD = "my_chat_member";
    private static final String CHATMEMBER_FIELD = "chat_member";
    private static final String CHATJOINREQUEST_FIELD = "chat_join_request";

    @JsonProperty(UPDATEID_FIELD)
    private Integer updateId;
    @JsonProperty(MESSAGE_FIELD)
    private Message message; ///< Optional. New incoming message of any kind — text, photo, sticker, etc.
    @JsonProperty(EDITEDMESSAGE_FIELD)
    private Message editedMessage; ///< Optional. New version of a message that is known to the bot and was edited
    @JsonProperty(CHANNELPOST_FIELD)
    private Message channelPost; ///< Optional. New incoming channel post of any kind — text, photo, sticker, etc.
    @JsonProperty(EDITEDCHANNELPOST_FIELD)
    private Message editedChannelPost; ///< Optional. New version of a channel post that is known to the bot and was edited
    
    /**
     * Optional.
     *
     * The bot's chat member status was updated in a chat.
     * For private chats, this update is received only when the bot is blocked or unblocked by the user.
     */
    @JsonProperty(MYCHATMEMBER_FIELD)
    private ChatMemberUpdated myChatMember;
    /**
     * Optional.
     *
     * A chat member's status was updated in a chat.
     * The bot must be an administrator in the chat and must explicitly specify “chat_member” in the list of allowed_updates to receive these updates.
     */
    @JsonProperty(CHATMEMBER_FIELD)
    private ChatMemberUpdated chatMember;
    @JsonProperty(CHATJOINREQUEST_FIELD)
    private ChatJoinRequest chatJoinRequest;

    public boolean hasMessage() {
        return message != null;
    }

    public boolean hasEditedMessage() {
        return editedMessage != null;
    }

    public boolean hasChannelPost() {
        return channelPost != null;
    }

    public boolean hasEditedChannelPost() {
        return editedChannelPost != null;
    }

    public boolean hasMyChatMember() {
        return myChatMember != null;
    }

    public boolean hasChatMember() {
        return chatMember != null;
    }

    public boolean hasChatJoinRequest() {
        return chatJoinRequest != null;
    }
}
