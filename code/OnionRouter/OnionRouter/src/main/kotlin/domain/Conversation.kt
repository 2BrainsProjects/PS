package domain

data class Conversation(
    val contactId: Int,
    val messages: List<Message>
)

/*
e.g.
{
    contactId: 4
    messages:[
        {
            conversationId: 3415,
            content: "olá",
            timestamp: "17/05/2024 10:01.14"
        }
    ]
}
*/