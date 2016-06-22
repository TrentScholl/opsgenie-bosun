# opsgenie-bosun
OpsGenie Marid integration for Bosun

The Marid Groovy script in this repository allows you to Acknowledge, Close and Delete Bosun alerts from OpsGenie

For this script to work, you will need to send alerts from Bosun to OpsGenie via Email.

Two email integration aliases are required, one for warning notifications and another for critical notifications.
Both email integrations need the Bosun incident ID and severity to be assigned to the alias field in OpsGenie. This can be done by adding the incident ID to your template subject like so:

    subject = [#{{.Id}}] - {{.Last.Status}} - {{.Alert.Name}}

The OpsGenie string manipulation methods can then be used to extract the ID in to the alias field. For the warning integration you need to use:

    {{ subject.substringBetween("[#","]") }}-warning

For the critical integration you need to use:

    {{ subject.substringBetween("[#","]") }}-critical
