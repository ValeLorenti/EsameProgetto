package it.uniroma3.siw.taskmanager.authentication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;

import static it.uniroma3.siw.taskmanager.model.Credentials.ADMIN_ROLE;

/*extends WebSecurityAdapter che usa una compenente di spring (datasource) che serve per modellare un punto d'accesso al db*/
@Configuration
@EnableWebSecurity
public class AuthConfiguration extends WebSecurityConfigurerAdapter {


	@Autowired
	DataSource datasource;

	/*implementiamo due metodi
	 * configure(HttpSecurity http) -> in cui definiamo le policies di autenticazione e autorizzazione
	 * 
	 * configure(AuthenticationManagerBuilder Auth) -> specifica al sistema dove trova le credenziali e i ruoli nel db
	 */

	/*questo metodo conterrá una serie di concatenazioni all'oggetto http
	 * --> NB i me todi http restituiscono lo stesso oggetto http MA aggiornato (per questo le concatenazioni)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		/*definiamo chi é autorizzato a vedere cosa*/
		.authorizeRequests()

		/*tutti possono accedere alla pagina di login di registrazione e alla home*/
		.antMatchers(HttpMethod.GET, "/", "/index", "/login", "/users/register").permitAll()

		/*tutti possono mandare una richiesta post (equivale a dire -> invio dati)
		 * alle pagine di registrazione e di login
		 */		
		.antMatchers(HttpMethod.POST, "/login", "/users/register").permitAll()

		/*solo gli utenti autenticati come ADMIN possono accedere alla admin page*/
		.antMatchers(HttpMethod.GET, "/admin/**").hasAnyAuthority(ADMIN_ROLE)
		.antMatchers(HttpMethod.POST, "/admin/**").hasAnyAuthority(ADMIN_ROLE)

		/*tutti gli utenti autenticati possono accedere alle altre pagine*/
		.anyRequest().authenticated()

		/*notiamo come per chiedere la vista del login usiamo una get, al contrario per autenticarsi
		 * usiamo una post.
		 * Una volta autenticati la pagina di default é la home
		 */
		.and().formLogin()

		// after login is successful, redirect to the logged user homepage
		.defaultSuccessUrl("/home")

		/*per fare logout si manda una richiesta http GET a /logout
		 * --> si viene quindi deautenticati e si ritorna ad index
		 */
		.and().logout()
		.logoutUrl("/logout")               // logout is performed when sending a GET to "/logout"
		.logoutSuccessUrl("/index")      // after logout is successful, redirect to /index page
		.invalidateHttpSession(true)
		.clearAuthentication(true).permitAll();
	}

	/*in questo metodo si va a specificare dove l'applicazione deve andare a 
	 * trovare i dati che gli servono per l'autenticazione
	 * -> anche qui con la concatenazione
	 */
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
		
		/*viene usata la datasource per interfacciarsi con db
		 * -> viene usata come punto d'accesso
		 */
		.dataSource(this.datasource)

		/*viene fatto il retrive dell'username e del ruolo attraverso il datasource*/
		.authoritiesByUsernameQuery("SELECT user_name, role FROM credentials WHERE user_name=?")

		/*viene fatto il retrive dell'username, della password e 
		 * un flag booleano che specifica se l'utente é abilitato o meno
		 */
		.usersByUsernameQuery("SELECT user_name, password, 1 as enabled FROM credentials WHERE user_name=?");
	}

	@Bean /*un oggetto generato da bean rimane nel contesto dell'applicazione*/
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/*NB --> LOGIN -> in questo modo nel login viene cifrata la pwd inmesssa dallo user
	 * 		 REGISTER -> nella regitrazione dobbiamo dire noi al sistema di fare la cifratura della pwd 
	 * 					lo facciamo in credentialsService
	 */
}