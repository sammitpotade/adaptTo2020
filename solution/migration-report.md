# AEM Assets as a Cloud Service - Workflow Migration Report
This report contains the changes that have been made to your Maven project by the workflow migration tool.  After reviewing these changes, you should be able to simply run a `mvn clean install` command against your reactor POM file to build your project with these changes in place.  After validating these changes on a local developer environment, check these changes into your source control repository and run a Cloud Manager build to deploy these changes to your AEM in the Cloud environments.

## Maven Projects Added and Modified
Changes to your workflow models and launchers have been made in-place.  We have also added the following new Maven projects:

| Action | Project |
| ------ | ------- |
| Created | aem-cloud-migration.content |
| Created | aem-cloud-migration.apps |

These projects have been added as modules to your reactor POM.  Feel free to merge the content from these projects into your existing projects if you'd like, but note that in AEM Assets as a Cloud Service deployments, we require that immutable content (under /apps or /libs) be deployed separately from mutable content (under /conf, /content, /etc, /var).  As a result, _mixed_ content packages are not supported.

## Workflow Launchers
We have disabled some workflow launchers.  Depending on your source code, these will either be located under `/conf/global/settings/workflow/launcher/config` or `/etc/workflow/launcher/config`.  The Asset Compute Service will handle most asset processing in the cloud and any remaining custom workflow steps to be executed will be handled via the Custom Workflow Runner service.  We have disabled the following launchers:

| Action | Launcher |
| ------ | -------- |
| Disabled | `update_asset_mod` |
| Disabled | `update_asset_create` |

Note that in AEM Assets as a Cloud Service environments, out-of-the-box workflow launchers for DAM Update Asset and DAM Metadata Writeback workflows are disabled by default.

## Custom Workflow Runner configuration
We have created a configuration for the Custom Workflow Runner service in the `aem-cloud-migration.apps` project.  This service replaces the role of traditional workflow launchers for asset processing workflows in AEM Assets as a Cloud Service deployments.  Since the Asset Compute Service will be processing all of our image renditions, we can no longer rely on a simple JCR event to execute our workflows.  Instead, this service will be invoked when the Asset Compute Service has completed processing and the assets are ready for custom steps to be executed against them.  If you use Dynamic Media, note that we will upload assets to Dynamic Media before executing these workflows.  We have configured the following workflows:

| Action | Glob Pattern | Workflow Model |
| ------ | ------------ | -------------- |
| Created | `/content/dam(/((?!/subassets)(?!/marketing/seasonal).)*)` | `/var/workflow/models/dam/update_asset` |

Note that when using the `postProcWorkflowsByExpression` method of configuring this service, the glob patterns used will differ slightly from the patterns used for workflow launchers.  This is due to the fact that while workflow launchers usually target the original asset binary, the Custom Workflow Runner targets the asset itself.

## Workflow Model Updates
Many workflow models will combine Adobe-provided asset processing steps with custom customer-defined steps.  In these cases, we have removed most of the Adobe-provided steps from the workflow and configured the workflow to be executed by the Custom Workflow Runner.  When executing workflows via the Custom Workflow Runner service, we require that the workflow end with the DAM Update Asset Completed Process workflow step.  This will mark the asset as _Complete_.  As a result, we have added this step in any workflows where it was missing. 

We have made changes to the following workflow models:

### /conf/global/settings/workflow/models/dam/update_asset

| Action | Step | Reason |
| ------ | ---- | ------ |
| Removed | `com.day.cq.dam.core.process.GateKeeperProcess` | This process is not necessary in AEM Assets as a Cloud Service. |
| Removed | `com.day.cq.dam.core.process.MetadataProcessorProcess` | This functionality is provided by the Asset Compute Service. |
| Removed | `com.day.cq.dam.video.FFMpegThumbnailProcess` | This configuration has been migrated to a processing profile for the Asset Compute Service. |
| Removed | `com.day.cq.dam.core.process.CommandLineProcess` | This process is not currently supported in AEM Assets as a Cloud Service. |
| Removed | `com.day.cq.dam.core.process.CreatePdfPreviewProcess` | This configuration has been migrated to a processing profile for the Asset Compute Service. |
| Removed | `com.day.cq.dam.core.process.ThumbnailProcess` | This configuration has been migrated to a processing profile for the Asset Compute Service. |


## Asset Compute Service Processing Profiles
By inspecting your workflow step configurations, we were able to automatically generate processing profiles for the Asset Compute Service to encompass configurations that you have customized.  These configurations can be found in the (`aem-cloud-migration.content`) project.

| Action | Profile |
| ------ | ------- |
| Created | Migrated from update_asset |

Note that while we have created processing profiles from your workflow model configurations, we are unable to attach these profiles to your existing content structures.  After deploying these profiles to your environment, please visit /mnt/overlay/dam/gui/content/processingprofiles/processingprofiles.html to attach these configurations to one or more folders in your repository.