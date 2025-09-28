<script lang="ts">
	import { toast } from 'svelte-sonner';
	import { backendUrl } from '../util/SabanConfig';
	import type { PaginatedPronunciationResponse } from '../data/responses/ResponseInterfaces';
	import * as Table from '$lib/components/ui/table/index.js';
	import { Label } from '@/components/ui/label';
	import Button from '@/components/ui/button/button.svelte';
	import { onMount } from 'svelte';
	import SearchableCombobox from './SearchableCombobox.svelte';
	import { Languages } from '../data/Languages';

	let offset: number = $state(0);
	let limit: number = $state(10);
	let selectedLanguage: string | null = $state(null);

	let requests: PaginatedPronunciationResponse = $state({ totalCount: 0, data: [] });

	onMount(() => {
		fetchRequests();
	});

	$effect(() => {
		selectedLanguage;

		fetchRequests();
	});

	async function fetchRequests() {
		const resp = await fetch(`${backendUrl}/gui/request/paginated`, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-type': 'application/json; charset=UTF-8'
			},
			body: JSON.stringify({
				offset: offset,
				limit: limit,
				language: selectedLanguage
			})
		});

		if (resp.ok) {
			requests = await resp.json();
		}
	}
</script>

<div class="flex min-w-[500px] max-w-[500px] flex-col place-items-center gap-2 rounded-md border">
	<Label class="text-xl">Requested Pronunciations</Label>

	<div class="flex w-full items-center justify-between pl-2 pr-2">
		<Label class="">Help pronounce words for others</Label>
		<div class="flex items-center gap-1">
			<Label>Language:</Label>
			<SearchableCombobox options={Object.values(Languages)} bind:selected={selectedLanguage} />
		</div>
	</div>

	<Table.Root>
		<Table.Header>
			<Table.Row>
				<Table.Head class="">Requested by</Table.Head>
				<Table.Head class="">Language</Table.Head>
				<Table.Head>Word/Phrase</Table.Head>
				<Table.Head></Table.Head>
			</Table.Row>
		</Table.Header>
		<Table.Body>
			{#each requests.data as r}
				<Table.Row>
					<Table.Cell class="">{r.requestedBy}</Table.Cell>
					<Table.Cell class="">{r.language}</Table.Cell>
					<Table.Cell>{r.text}</Table.Cell>
					<Table.Cell class="text-right">
						<Button variant="outline">Pronounce</Button>
					</Table.Cell>
				</Table.Row>
			{/each}
		</Table.Body>
	</Table.Root>
</div>
