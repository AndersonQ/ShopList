/**
 * SettingsActivity.java is part of ShopList.
 *
 * ShopList is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ShopList is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ShopList.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author	Anderson de Franca Queiroz
 * @email	contato@andersonq.eti.br
 */
public class SettingsActivity extends PreferenceActivity 
{
	public static final String KEY_AUTO_REMOVE = "pref_auto_remove";
	public static final String KEY_PRICE_COMPARISON = "pref_price_comparison";
	public static final String KEY_AUTO_COMPLETE = "pref_auto_complete";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty);
		// Display the settings fragment as the main content
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new SettingsFrag())
                .commit();
		//addPreferencesFromResource(R.xml.preferences);
	}
}
