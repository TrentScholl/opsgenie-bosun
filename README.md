# opsgenie-bosun
OpsGenie Marid integration for Bosun

The Marid Groovy script in this repository allows you to Acknowledge, Close and Delete Bosun alerts from OpsGenie

For this script to work, you will need to send alerts from Bosun to OpsGenie via Email. The Bosun alert ID needs to be assigned to the alias field in OpsGenie.
This can be done by adding the alert ID to your template subject like so:

    subject = [#ID-{{.Id}}] {{.Last.Status}} - {{.Alert.Name}}

The OpsGenie string manipulation methods can then be used to extract the ID. eg:

    {{ subject.substringBetween("[#ID-","]") }}
