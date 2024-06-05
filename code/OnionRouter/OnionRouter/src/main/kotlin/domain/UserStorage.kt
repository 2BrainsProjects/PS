package domain

data class UserStorage(val id: Int, val name: String, val privateKey: String?, val contacts: List<Contact>)

/*
e.g.
{
    id:10,
    name:jose,
    privateKey:asdasd,
    contacts:[
        {
            id:32,
            name:joana
        },
        {
            id:35,
            name:diogo
        },
    ]
}
*/
