package club.zori.event.eventbus

import club.zori.event.listener.AsyncListener

interface IAsyncEventBus : IEventBus {

    /**
     * A map for events and their subscribed listeners
     *
     * <Event, Set<Listener>>
     */
    val subscribedListenersAsync: MutableMap<Class<*>, MutableSet<AsyncListener<*>>>

    /**
     * Called when putting a new set to the map
     */
    fun newSetAsync(): MutableSet<AsyncListener<*>>

}