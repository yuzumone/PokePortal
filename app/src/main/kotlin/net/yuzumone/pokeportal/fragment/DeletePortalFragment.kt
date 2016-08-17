/*
 * Copyright (C) 2016 yuzumone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.yuzumone.pokeportal.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import net.yuzumone.pokeportal.listener.OnDeletePortal

class DeletePortalFragment : DialogFragment() {

    lateinit private var mListener: OnDeletePortal
    lateinit private var mName: String
    lateinit private var mUuid: String

    companion object {
        val ARG_NAME = "name"
        val ARG_UUID = "uuid"
        fun newInstance(name: String, uuid: String): DeletePortalFragment {
            val fragment = DeletePortalFragment()
            val args = Bundle()
            args.putString(ARG_NAME, name)
            args.putString(ARG_UUID, uuid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context !is OnDeletePortal) {
            throw ClassCastException("Don't implement Listener.")
        }
        mListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mName = arguments.getString(ARG_NAME)
        mUuid = arguments.getString(ARG_UUID)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setMessage("Delete $mName?")
                .setPositiveButton("OK") { dialog, which ->
                    mListener.deletePortal(mUuid)
                    dismiss()
                }
                .setNegativeButton("CANCEL") { dialog, which -> dismiss() }
                .create()
    }
}