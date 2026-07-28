    private suspend fun generateFromTrack(track: Track, context: EchoMediaItem?): Radio {
        // 1. Verificamos si venimos de un contexto con lista definida (como Top Songs)
        if (context is EchoMediaItem.Lists) {
            val listTracks = context.list.mapNotNull { media ->
                when (media) {
                    is EchoMediaItem.TrackItem -> media.track
                    else -> null
                }
            }

            if (listTracks.isNotEmpty()) {
                // Buscamos la posición de la canción que tocaste
                val selectedIndex = listTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

                // Armamos la lista para que empiece en la canción seleccionada y continúe con las siguientes
                val orderedTracks = listTracks.subList(selectedIndex, listTracks.size) + 
                                    listTracks.subList(0, selectedIndex)

                val id = "custom_list_${track.id}"
                return Radio(
                    id = id,
                    title = context.title ?: "Top Songs",
                    extras = mutableMapOf(
                        "tracks" to json.encodeToString(orderedTracks)
                    )
                )
            }
        }

        // 2. Si no venimos de una lista fija, se ejecuta el comportamiento original de YouTube Radio
        val id = "radio_${track.id}"
        val cont = context?.extras?.get("cont")
        val result = api.SongRadio.getSongRadio(track.id, cont).getOrThrow()
        val tracks = result.items.map { song: dev.toastbits.ytmkt.model.external.mediaitem.YtmSong -> 
            song.toTrack(thumbnailQuality)
        }
        
        return Radio(
            id = id,
            title = "${track.title} Radio",
            extras = mutableMapOf<String, String>().apply {
                put("tracks", json.encodeToString(tracks))
                result.continuation?.let { put("cont", it) }
            }
        )
    }
    
