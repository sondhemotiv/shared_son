package org.emotiv.cortex

class CortexInstallerBuild implements Serializable {
    def steps = null

    CortexInstallerBuild(steps) {
        this.steps = steps
    }

    def build_mac_cortex_installer_online_shared(cortex_job_name, cortexui_job_name, pro_job_name, bci_job_name, brainviz_job_name, installer_repo, installer_file_name)
    {
        script { echo "Build Mac Cortex Installer Online Shared" }
            
    }
}