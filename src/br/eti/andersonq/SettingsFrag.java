/**
 * SettingsFrag.java is part of ShopList.
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
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author	Anderson de Franca Queiroz
 * @email	contato@andersonq.eti.br
 */
public class SettingsFrag extends PreferenceFragment 
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
