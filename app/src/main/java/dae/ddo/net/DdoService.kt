package dae.ddo.net

import androidx.annotation.Keep
import dae.ddo.net.retro.CompendiumRetro
import dae.ddo.net.retro.ServerGroupRetro
import retrofit2.Response
import retrofit2.http.GET

@Keep
interface DdoService {

    // https://www.playeraudit.com/api/compendium
    @GET("compendium")
    suspend fun compendium(): Response<List<CompendiumRetro>>

    // https://www.playeraudit.com/api/groups
    @GET("groups")
    suspend fun serverGroups(): Response<List<ServerGroupRetro>>

}