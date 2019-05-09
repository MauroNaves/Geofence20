package com.example.geofence20.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

object FragmentUtil {

	@JvmStatic
	fun substituirFragment(
		fragmentManager: FragmentManager?,
		fragment: Fragment,
		idLayout: Int,
		manterPilhaHistorico: Boolean
	) {

		val frag: Fragment?
		if (fragmentManager == null) {
			return
		}

		val nomeFragment = fragment.javaClass.simpleName
		val transaction = fragmentManager.beginTransaction()
//		transaction.setCustomAnimations(
//			android.R.anim.slide_in_left,
//			android.R.anim.slide_out_right,
//		    android.R.animator.fade_in,
//		    android.R.animator.fade_out
//		)

		frag = fragmentManager.findFragmentByTag(nomeFragment)


		if (manterPilhaHistorico && frag == null) {
			transaction.addToBackStack(nomeFragment)
		}

		if (frag != null) {
			fragmentManager.popBackStack(nomeFragment, 0)

		} else {
			transaction.replace(idLayout, fragment, nomeFragment).commit()
		}

	}

}