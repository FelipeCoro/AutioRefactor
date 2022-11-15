package com.autio.android_app

class Animation {

    private lateinit var animator: Thread

    init {
        animator = Thread {

        }
    }

    fun startAnimation() {
        animator.start()
    }

    @Throws(InterruptedException::class)
    fun awaitCompletion()  {
        animator.join()
    }
}