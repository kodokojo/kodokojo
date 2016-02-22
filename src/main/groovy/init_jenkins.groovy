import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.impl.*
import jenkins.model.Jenkins

//https://github.com/hayderimran7/useful-jenkins-groovy-init-scripts/blob/master/useful_groovy_snippets.groovy
private credentials_for_username(String username) {
    def username_matcher = CredentialsMatchers.withUsername(username)
    def available_credentials =
            CredentialsProvider.lookupCredentials(
                    StandardUsernameCredentials.class,
                    Jenkins.getInstance(),
                    hudson.security.ACL.SYSTEM,
                    new SchemeRequirement("ssh")
            )

    return CredentialsMatchers.firstOrNull(
            available_credentials,
            username_matcher
    )
}

/////////////////////////
// create or update user
/////////////////////////
void create_or_update_user(String user_name, String email, String password = "", String full_name = "", String public_keys = "") {
    def user = hudson.model.User.get(user_name)
    user.setFullName(full_name)

    def email_param = new hudson.tasks.Mailer.UserProperty(email)
    user.addProperty(email_param)

    def pw_param = hudson.security.HudsonPrivateSecurityRealm.Details.fromPlainPassword(password)
    user.addProperty(pw_param)

    if (public_keys != "") {
        def keys_param = new org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl(public_keys)
        user.addProperty(keys_param)
    }

    user.save()
}

/////////////////////////
// create credentials
/////////////////////////
void create_or_update_credentials(String username, String password, String description = "", String private_key = "") {
    def global_domain = Domain.global()
    def credentials_store =
            Jenkins.instance.getExtensionList(
                    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
            )[0].getStore()

    def credentials = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            null,
            description,
            username,
            password
    )

    // Create or update the credentials in the Jenkins instance
    def existing_credentials = credentials_for_username(username)

    if (existing_credentials != null) {
        credentials_store.updateCredentials(
                global_domain,
                existing_credentials,
                credentials
        )
    } else {
        credentials_store.addCredentials(global_domain, credentials)
    }
}


def strategy = new hudson.security.FullControlOnceLoggedInAuthorizationStrategy()
def realm = new hudson.security.HudsonPrivateSecurityRealm(false, false, null)

instance.setAuthorizationStrategy(strategy)
instance.setSecurityRealm(realm)

create_or_update_credentials('jpthiery', 'jpascal', "", "MYPRivateKey")

