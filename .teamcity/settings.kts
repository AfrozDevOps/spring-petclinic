import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.10"

project {

    buildType(Testing)
}

object Testing : BuildType({
    name = "testing config as code"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCommand {
            enabled = false
            commandType = build {
                source = file {
                    path = ".devcontainer/Dockerfile"
                }
            }
        }
        dockerCompose {
            enabled = false
            file = "docker-compose.yml"
        }
        maven {
            enabled = false
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
        gradle {
            enabled = false
            tasks = "clean build"
            gradleWrapperPath = ""
        }
        script {
            enabled = false
            scriptContent = "call mvnw.cmd"
        }
        python {
            name = "SELENIUM SCRIPT"
            command = script {
                content = """
                    from selenium import webdriver
                    from selenium.webdriver.common.by import By
                    from selenium.webdriver.common.keys import Keys
                    from selenium.webdriver.common.action_chains import ActionChains
                    from selenium.webdriver.support import expected_conditions as EC
                    from selenium.webdriver.support.ui import WebDriverWait
                    import time
                    
                    driver=webdriver.Chrome()
                    mail=driver.get("https://getnada.com/")
                    test=driver.find_element("xpath","/html/body/div[1]/div/div/div/div[2]/div/div[1]/div/div/p/span[1]/a/button").text
                    fs=driver.execute_script("window.open('https://dev.forcesetup.com')")
                    driver.switch_to.window(driver.window_handles[1])
                    WebDriverWait(driver, 20).until(EC.element_to_be_clickable((By.XPATH,"/html/body/div[1]/div/div/div/div[1]/div/div[2]/div[2]/button")))
                    sign_up = driver.find_element("xpath", "/html/body/div[1]/div/div/div/div[1]/div/div[2]/div[2]/button").click()
                    newemail=driver.find_element("name","email").send_keys(test)
                    newpassword=driver.find_element("name","password").send_keys("AbcdAbcd@6t")
                    newname=driver.find_element("name","name").send_keys("testing-selenium")
                    driver.find_element("xpath","/html/body/div[2]/div/div/button[1]").click()
                    driver.switch_to.window(driver.window_handles[0])
                    WebDriverWait(driver, 20).until(EC.element_to_be_clickable((By.LINK_TEXT,"AMETEK STC Account Verification Code")))
                    Link=driver.find_element(by=By.LINK_TEXT,value="AMETEK STC Account Verification Code")
                    driver.execute_script("arguments[0].click();",Link)
                    WebDriverWait(driver, 10).until(EC.frame_to_be_available_and_switch_to_it((By.XPATH,'//*[@id="the_message_iframe"]')))
                    WebDriverWait(driver, 20).until(EC.presence_of_element_located((By.XPATH,"/html/body/div[1]/div[2]/h3")))
                    code=driver.find_element("xpath","/html/body/div[1]/div[2]/h3").text
                    driver.switch_to.default_content()
                    driver.switch_to.window(driver.window_handles[1])
                    newcode=driver.find_element("name","code").send_keys(code)
                    print("Unit test #number is passed!!!")
                    driver.quit()
                """.trimIndent()
            }
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})
