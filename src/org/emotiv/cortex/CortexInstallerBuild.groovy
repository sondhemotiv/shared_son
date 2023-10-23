package org.emotiv.cortex

@NonCPS
class CortexInstallerBuild implements Serializable {
    def steps = null

    CortexInstallerBuild(steps) {
        this.steps = steps
    }

    def build_mac_cortex_installer_online_shared(cortex_job_name, cortexui_job_name, pro_job_name, bci_job_name, brainviz_job_name, installer_repo, installer_file_name)
    {
        script {
            def S3_REPO_URL
            def CORTEX_BUILD_ID = httpRequest authentication: 'hello_token',
                            url: "${JENKINS_URL}job/${cortex_job_name}/lastSuccessfulBuild/buildNumber"
            CORTEX_BUILD_ID = "${CORTEX_BUILD_ID.content}"
            echo "Cortex build id: ${CORTEX_BUILD_ID}"
            def CORTEXUI_BUILD_ID = httpRequest authentication: 'hello_token',
                            url: "${JENKINS_URL}job/${cortexui_job_name}/lastSuccessfulBuild/buildNumber"
            CORTEXUI_BUILD_ID = "${CORTEXUI_BUILD_ID.content}"
            echo "Cortex UI build id: ${CORTEXUI_BUILD_ID}"
            def PRO_BUILD_ID = httpRequest authentication: 'hello_token',
                            url: "${JENKINS_URL}job/${pro_job_name}/lastSuccessfulBuild/buildNumber"
            PRO_BUILD_ID = "${PRO_BUILD_ID.content}"
            echo "Emotiv Pro build id: ${PRO_BUILD_ID}"
            def BCI_BUILD_ID = httpRequest authentication: 'hello_token',
                            url: "${JENKINS_URL}job/${bci_job_name}/lastSuccessfulBuild/buildNumber"
            BCI_BUILD_ID = "${BCI_BUILD_ID.content}"
            echo "Emotiv BCI build id: ${BCI_BUILD_ID}"
            def BRAINVIZ_BUILD_ID = httpRequest authentication: 'hello_token',
                            url: "${JENKINS_URL}job/${brainviz_job_name}/lastSuccessfulBuild/buildNumber"
            BRAINVIZ_BUILD_ID = "${BRAINVIZ_BUILD_ID.content}"
            echo "Emotiv BrainViz build id: ${BRAINVIZ_BUILD_ID}"

            def CORTEX_ARTIFACTS_JOB_NAME = "${cortex_job_name}".replaceAll(/\/job/, '')
            if (BUILD_TYPE == 'develop')
            {
                sh 'sh clean_for_mac.sh "dev"'
            }
            else
            {
                sh 'sh clean_for_mac.sh "pro"'
            }
            copyArtifacts(
                projectName: "${CORTEX_ARTIFACTS_JOB_NAME}",
                filter: 'build-jenkins-MacOS*/**',
                selector: lastSuccessful(),
                target: "${WORKSPACE}"
            )
            //TODO: use copyArtifacts for another project
            sh """
            rm -f version.txt
            echo -Cortex-${CORTEX_BUILD_ID}-UI-${CORTEXUI_BUILD_ID}-PRO-${PRO_BUILD_ID}-BCI-${BCI_BUILD_ID}-BV-${BRAINVIZ_BUILD_ID}>> version.txt
            """

            sh """
            cp -r build-jenkins-MacOS-${CORTEX_BUILD_ID}/output/bin/ packages/com.emotiv.cortex/data/
            cp -r "${JENKINS_ROOT_DIR}/workspace/${cortexui_job_name}/build-jenkins-cortexui-${CORTEXUI_BUILD_ID}/EMOTIV ${EAPP_NAME}.app" packages/com.emotiv.cortex/data/
            cp -r ${JENKINS_ROOT_DIR}/workspace/${cortexui_job_name}/build-jenkins-cortexui-${CORTEXUI_BUILD_ID}/EMOTIV-Applet.app packages/com.emotiv.cortex/data/
            cp -r ${JENKINS_ROOT_DIR}/workspace/${cortexui_job_name}/build-jenkins-cortexui-${CORTEXUI_BUILD_ID}/crashpad_handler packages/com.emotiv.cortex/data/

            cp -r ${JENKINS_ROOT_DIR}/workspace/${pro_job_name}/build-jenkins-emotivpro-${PRO_BUILD_ID}/EmotivPRO.app packages/com.emotiv.emotivpro/data/
            cp -r ${JENKINS_ROOT_DIR}/workspace/${bci_job_name}/build-jenkins-emotivbci-${BCI_BUILD_ID}/EmotivBCI.app packages/com.emotiv.emotivbci/data/
            cp -r "${JENKINS_ROOT_DIR}/workspace/${brainviz_job_name}/build-jenkins-${BRAINVIZ_BUILD_ID}/MacOS/Emotiv BrainViz.app" packages/com.emotiv.emotivbrainviz/data/
            """

            sh """
            cp -rf packages/com.emotiv.cortex/data/CortexService.app/Contents/MacOS/* "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/MacOS/"
            cp -rf packages/com.emotiv.cortex/data/CortexService.app/Contents/PlugIns/servicebackends "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/PlugIns/"

            cp -rf packages/com.emotiv.cortex/data/CortexService.app/Contents/Frameworks/* "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/"
            cp -rf packages/com.emotiv.cortex/data/BluetoothHelper.framework "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/"
            rm -rf packages/com.emotiv.cortex/data/BluetoothHelper.framework
            cp -rf packages/com.emotiv.emotivpro/data/EmotivPRO.app/Contents/Frameworks/* "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/"
            cp -rf packages/com.emotiv.emotivbci/data/EmotivBCI.app/Contents/Frameworks/* "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/"
            rm -rf packages/com.emotiv.cortex/data/CortexService.app/Contents/Frameworks/
            rm -rf packages/com.emotiv.emotivpro/data/EmotivPRO.app/Contents/Frameworks/
            rm -rf packages/com.emotiv.emotivbci/data/EmotivBCI.app/Contents/Frameworks/

            cp -rf packages/com.emotiv.cortex/data/CortexService.app/Contents/PlugIns/servicebackends "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/PlugIns/"

            rm -rf packages/com.emotiv.cortex/data/CortexService.app
            rm -rf packages/com.emotiv.maintenance/data/README.txt
            """

            sh """
            echo handling framework of EMOTIV App
            rm -r "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/QtWebEngineCore.framework/Resources/qtwebengine_locales"
            rm -r "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/QtWebEngineCore.framework/Resources/qtwebengine_devtools_resources.pak"
            """

            sh '''
            for frameworkName in \$(find "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/" -maxdepth 1 -mindepth 1 -type d -execdir basename '{}' ';')
            do
                echo fixing $frameworkName
                if [ "\$frameworkName" == 'QtWebEngineCore.framework' ];
                then
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName"
                    cp -r  "/Users/sdkteam/Qt/${QT_DEV_VERSION}/macos/lib/$frameworkName" "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Versions/A/Headers"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Versions/A/Resources/qtwebengine_devtools_resources.pak"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Versions/A/Resources/qtwebengine_locales"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Headers"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Helpers"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/QtWebEngineCore"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Resources"
                    rm -rf "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Versions/Current"
                else
                    for versionName in \$(find "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Versions/" -maxdepth 1 -mindepth 1 -type d -execdir basename '{}' ';')
                    do
                        rm -r "packages/com.emotiv.cortex/data/EMOTIV ${EAPP_NAME}.app/Contents/Frameworks/$frameworkName/Versions/$versionName"/*
                    done
                fi
            done
            '''

            if (BUILD_TYPE == 'release')
            {
                script {
                    S3_REPO_URL = "s3://emotiv-app-archive/emotiv_installer/release/repo"
                    contentReplace(
                    configs: [
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'ENABLE_DEV_REPO', replace: '0', matchCount: 0),
                                fileContentReplaceItemConfig( search: 'ENABLE_RELEASE_REPO', replace: '1', matchCount: 0),
                                fileContentReplaceItemConfig(search: 'ENABLE_STAGING_REPO', replace: '0', matchCount: 0)],
                            filePath: 'config/config.xml', fileEncoding: 'UTF-8')])
                }
            }
            else if (BUILD_TYPE == 'staging')
            {
                script {
                    S3_REPO_URL = "s3://emotiv-app-archive/emotiv_installer/release/repo"
                    contentReplace(
                    configs: [
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'ENABLE_DEV_REPO', replace: '0', matchCount: 0),
                                fileContentReplaceItemConfig( search: 'ENABLE_RELEASE_REPO', replace: '0', matchCount: 0),
                                fileContentReplaceItemConfig(search: 'ENABLE_STAGING_REPO', replace: '1', matchCount: 0)],
                            filePath: 'config/config.xml', fileEncoding: 'UTF-8')])
                }
            }
            else
            {
                script {
                    S3_REPO_URL = "s3://emotiv-app-archive/emotiv_installer/staging/repo"
                    contentReplace(
                        configs: [
                            fileContentReplaceConfig(
                                configs: [ fileContentReplaceItemConfig( search: 'ENABLE_DEV_REPO', replace: '1', matchCount: 0),
                                    fileContentReplaceItemConfig( search: 'ENABLE_RELEASE_REPO', replace: '0', matchCount: 0),
                                    fileContentReplaceItemConfig(search: 'ENABLE_STAGING_REPO', replace: '0', matchCount: 0)],
                                filePath: 'config/config.xml', fileEncoding: 'UTF-8'),
                            fileContentReplaceConfig(
                                configs: [fileContentReplaceItemConfig( search: 'isDevVersion = false', replace: 'isDevVersion = true', matchCount: 0)],
                                filePath: 'config/controlscript.qs', fileEncoding: 'UTF-8')
                        ]
                    )

                }
            }

            script {
                contentReplace(
                    configs: [
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: '2.2.', replace: "${MAIN_VERSION}.", matchCount: 0),
                                fileContentReplaceItemConfig( search: '2019-10-07', replace: "${CURRENT_DATE}", matchCount: 0),
                                fileContentReplaceItemConfig(search: 'CORTEX_BUILD_NUMBER', replace: "${CORTEXUI_BUILD_ID}.${CORTEX_BUILD_ID}", matchCount: 0)],
                            filePath: 'packages/*/meta/package.xml', fileEncoding: 'UTF-8'),
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig(search: 'OS_NAME_LABEL', replace: 'Mac', matchCount: 0),
                                fileContentReplaceItemConfig(search: 'Repo_Name', replace: "${installer_repo}", matchCount: 0),
                                fileContentReplaceItemConfig(search: 'EMOTIV_APP_BUILD_NUMBER', replace: "${CORTEX_BUILD_ID}", matchCount: 0)],
                            filePath: 'config/config.xml', fileEncoding: 'UTF-8'),
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'ALLOW_KEEPING_V1_WHILE_INSTALLING_V2', replace: 'false', matchCount: 0)],
                            filePath: 'config/controlscript.qs', fileEncoding: 'UTF-8'),
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'ALLOW_KEEPING_V1_WHILE_INSTALLING_V2', replace: 'false', matchCount: 0)],
                            filePath: 'packages/com.emotiv.cortex/meta/installscript.qs', fileEncoding: 'UTF-8'),
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'APP_BUILD_NUMBER', replace: "${PRO_BUILD_ID}", matchCount: 0)],
                            filePath: 'packages/com.emotiv.emotivpro/meta/package.xml', fileEncoding: 'UTF-8'),
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'APP_BUILD_NUMBER', replace: "${BCI_BUILD_ID}", matchCount: 0)],
                            filePath: 'packages/com.emotiv.emotivbci/meta/package.xml', fileEncoding: 'UTF-8'),
                        fileContentReplaceConfig(
                            configs: [
                                fileContentReplaceItemConfig( search: 'APP_BUILD_NUMBER', replace: "${BRAINVIZ_BUILD_ID}", matchCount: 0)],
                            filePath: 'packages/com.emotiv.emotivbrainviz/meta/package.xml', fileEncoding: 'UTF-8')
                    ]
                )
            }

            sh """
            echo ${installer_repo}>> packages/com.emotiv.cortex/data/installer_repo_name.txt
            """

            sh """
            ${QIF_MAC_PATH}/binarycreator -t /${QIF_MAC_PATH}/installerbase --online-only -c config/config.xml -p packages "${installer_file_name}" -rcc
            cp -rf update.rcc packages/com.emotiv.maintenance/data
            rm -rf ${installer_repo}
            /${QIF_MAC_PATH}/repogen -p packages ${installer_repo}
            """
            zip zipFile: "${installer_repo}.tar.gz", dir: "${installer_repo}", archive: false, overwrite: true
            sh """
            /${QIF_MAC_PATH}/binarycreator --online-only -c config/config.xml -p packages "${installer_file_name}"

            security unlock-keychain -p Sdk@123
            codesign --options "runtime" --deep ${installer_file_name}.app -s 'Developer ID Application: EMOTIV Inc. (X444Y2GQGP)'

            rm -rf ${installer_file_name}.dmg
            hdiutil create ${installer_file_name}.dmg -srcfolder ./${installer_file_name}.app/
            """

            sh """
            ${AWS_MAC_BIN} s3 rm ${S3_REPO_URL}/${installer_repo} --recursive
            ${AWS_MAC_BIN} s3 cp ${installer_repo} ${S3_REPO_URL}/${installer_repo} --acl public-read --recursive
            """
        }
    }
}