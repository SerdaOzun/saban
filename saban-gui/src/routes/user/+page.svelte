<script lang="ts">
	import Label from '@/components/ui/label/label.svelte';
	import { loggedInUser } from '../../data/stores/UserStore';
	import SearchableCombobox from '../../components/SearchableCombobox.svelte';
	import { countries } from '../../util/countries';
	import { backendUrl } from '../../util/SabanConfig';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import { Toaster } from '@/components/ui/sonner';

	let selectedCountry: string | null = $state(null);

	interface SettingsResponse {
		country: string | null;
	}

	onMount(() => {
		fetchCountry();
	});

	$effect(() => {
		selectedCountry;

		setCountry();
	});

	async function fetchCountry() {
		const resp = await fetch(`${backendUrl}/gui/settings/data`, {
			credentials: 'include'
		});

		if (resp.ok) {
			let settings: SettingsResponse = await resp.json();
			selectedCountry = settings.country;
		}
	}

	async function setCountry() {
		if (!selectedCountry) return;

		const resp = await fetch(`${backendUrl}/gui/settings/country/${selectedCountry}`, {
			method: 'POST',
			credentials: 'include'
		});

		if (resp.ok) {
            console.log("ok")
			toast.success('Country has been saved', {
				description: ''
			});
		}
	}
</script>

<div class="flex flex-col gap-2 p-2">
	{#if $loggedInUser}
		<Label class="text-2xl">Hello {$loggedInUser.username}</Label>
	{/if}
	<div class="flex w-fit flex-wrap place-items-center gap-2">
		<Label class="text-lg">Country:</Label>
		<SearchableCombobox options={countries} bind:selected={selectedCountry}></SearchableCombobox>
	</div>
</div>

<Toaster/>