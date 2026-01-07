<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { backendUrl } from '../util/SabanConfig';
	import type {
		PaginatedPronunciationResponse,
		PronunciationRequest
	} from '../data/responses/ResponseInterfaces';
	import * as Table from '$lib/components/ui/table/index.js';
	import { Label } from '@/components/ui/label';
	import Button from '@/components/ui/button/button.svelte';
	import { onMount } from 'svelte';
	import SearchableCombobox from './SearchableCombobox.svelte';
	import { Languages } from '../data/Languages';
	import * as Pagination from '$lib/components/ui/pagination/index.js';
	import { MediaQuery } from 'svelte/reactivity';
	import ChevronLeftIcon from '@lucide/svelte/icons/chevron-left';
	import ChevronRightIcon from '@lucide/svelte/icons/chevron-right';
	import AudioRecorder from './AudioRecorder.svelte';
	import RequestFulfillmentDialog from './RequestFulfillmentDialog.svelte';

	const isDesktop = new MediaQuery('(min-width: 768px)');

	let allLanguages = ['All', ...Object.values(Languages)];
	let selectedLanguage: string = $state(allLanguages[0]);

	let currentPage = $state(1);
	const perPage = $derived(isDesktop.current ? 10 : 5);
	const siblingCount = $derived(isDesktop.current ? 1 : 0);

	let requests: PaginatedPronunciationResponse = $state({ totalCount: 0, data: [] });
	let selected: PronunciationRequest | null = $state(null);
	let showDialog: boolean = $state(false);

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
				offset: (currentPage - 1) * perPage,
				limit: perPage,
				language: selectedLanguage === 'All' ? null : selectedLanguage
			})
		});

		if (resp.ok) {
			requests = await resp.json();
		}
	}
</script>

<div
	class="flex h-[600px] min-w-[500px] max-w-[500px] flex-col place-items-center gap-2 rounded-md border"
>
	<Label class="text-xl">Requested Pronunciations</Label>

	<div class="flex w-full items-center justify-between pl-2 pr-2">
		<Label class="">Help pronounce words for others</Label>
		<div class="flex items-center gap-1">
			<Label>Language:</Label>
			<SearchableCombobox options={allLanguages} bind:selected={selectedLanguage} />
		</div>
	</div>

	{#if requests.totalCount === 0}
		<Label class="mt-4 text-lg">No requests found for selected language</Label>
	{:else}
		<div class="w-full flex-1 overflow-hidden">
			<Table.Root class="w-full table-fixed">
				<Table.Header>
					<Table.Row>
						<Table.Head class="w-1/6">Language</Table.Head>
						<Table.Head class="w-3/6">Word/Phrase</Table.Head>
						<Table.Head></Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each requests.data as r}
						<Table.Row class="w-full">
							<Table.Cell>{r.language}</Table.Cell>
							<Table.Cell>
								<div class="line-clamp-2">
									{r.text}
								</div>
							</Table.Cell>
							<Table.Cell class="text-right">
								<Button
									variant="outline"
									onclick={() => {
										selected = r;
										showDialog = true;
									}}>Pronounce</Button
								>
							</Table.Cell>
						</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>
	{/if}

	{#if requests.totalCount > 0}
		<div class="mb-2 align-bottom">
			<Pagination.Root count={requests.totalCount} {perPage} {siblingCount} bind:page={currentPage}>
				{#snippet children({ pages, currentPage })}
					<Pagination.Content>
						<Pagination.Item>
							<Pagination.PrevButton>
								<ChevronLeftIcon class="size-4" />
								<span class="hidden sm:block">Previous</span>
							</Pagination.PrevButton>
						</Pagination.Item>
						{#each pages as page (page.key)}
							{#if page.type === 'ellipsis'}
								<Pagination.Item>
									<Pagination.Ellipsis />
								</Pagination.Item>
							{:else}
								<Pagination.Item>
									<Pagination.Link {page} isActive={currentPage === page.value}>
										{page.value}
									</Pagination.Link>
								</Pagination.Item>
							{/if}
						{/each}
						<Pagination.Item>
							<Pagination.NextButton>
								<span class="hidden sm:block">Next</span>
								<ChevronRightIcon class="size-4" />
							</Pagination.NextButton>
						</Pagination.Item>
					</Pagination.Content>
				{/snippet}
			</Pagination.Root>
		</div>
	{/if}
</div>

{#if selected}
	<RequestFulfillmentDialog
		bind:showDialog
		word={selected.text}
		selectedLanguage={selected.language}
		requestId={selected.id}
	/>
{/if}
