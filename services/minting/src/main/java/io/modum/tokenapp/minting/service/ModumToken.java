package io.modum.tokenapp.minting.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use {@link org.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version 2.2.1.
 */
public final class ModumToken extends Contract {
    private static final String BINARY = "6060604052600060038190556004819055600555629896806006556301c9c3806007556008805460ff19169055600f600955341561003c57600080fd5b5b60008054600160a060020a03191633600160a060020a03161790555b5b610ed6806100696000396000f300606060405236156100d85763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde038114610163578063095ea7b3146101ee57806318160ddd1461022457806323b872dd1461024957806340c10f19146102855780634b9f5c98146102a957806350635394146102d357806370a08231146102e857806375143ef2146103195780638737ecee1461032e57806395d89b4114610388578063a9059cbb14610413578063b0a853de14610449578063dd62ed3e1461045e578063e74988d814610495575b6101615b6000806100eb346004546104ba565b9150600554828115156100fa57fe5b06600481905550610118610110836004546104d4565b6005546104eb565b9050610126600354826104ba565b6003557f22427047e1a674a9aff59700a2c8d00ea96e017ddf9236690bdedf1f21f28d9d8160405190815260200160405180910390a15b5050565b005b341561016e57600080fd5b610176610507565b60405160208082528190810183818151815260200191508051906020019080838360005b838110156101b35780820151818401525b60200161019a565b50505050905090810190601f1680156101e05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156101f957600080fd5b610210600160a060020a036004351660243561053e565b604051901515815260200160405180910390f35b341561022f57600080fd5b6102376105ab565b60405190815260200160405180910390f35b341561025457600080fd5b610210600160a060020a03600435811690602435166044356105c1565b604051901515815260200160405180910390f35b341561029057600080fd5b610161600160a060020a0360043516602435610724565b005b34156102b457600080fd5b61023760043515156107e9565b60405190815260200160405180910390f35b34156102de57600080fd5b6101616108b3565b005b34156102f357600080fd5b610237600160a060020a036004351661090f565b60405190815260200160405180910390f35b341561032457600080fd5b610161610931565b005b341561033957600080fd5b61016160046024813581810190830135806020601f820181900481020160405190810160405281815292919060208401838380828437509496505084359460200135935061096c92505050565b005b341561039357600080fd5b610176610a3f565b60405160208082528190810183818151815260200191508051906020019080838360005b838110156101b35780820151818401525b60200161019a565b50505050905090810190601f1680156101e05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561041e57600080fd5b610210600160a060020a0360043516602435610a76565b604051901515815260200160405180910390f35b341561045457600080fd5b610161610b51565b005b341561046957600080fd5b610237600160a060020a0360043581169060243516610c47565b60405190815260200160405180910390f35b34156104a057600080fd5b610237610c74565b60405190815260200160405180910390f35b6000828201838110156104c957fe5b8091505b5092915050565b6000828211156104e057fe5b508082035b92915050565b60008082848115156104f957fe5b0490508091505b5092915050565b60408051908101604052600b81527f4d6f64756d20546f6b656e000000000000000000000000000000000000000000602082015281565b600160a060020a03338116600081815260026020908152604080832094871680845294909152808220859055909291907f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259085905190815260200160405180910390a35060015b92915050565b60006105bb6005546006546104ba565b90505b90565b60085460009081908190819060ff1615156105db57600080fd5b6105e6336000610c7b565b9250848360040154101580156105fc5750600085115b801561062f5750600160a060020a0380881660009081526002602090815260408083203390941683529290522054859010155b156107155761063f336003610c7b565b915061064c866003610c7b565b905061065c8260040154866104d4565b82600401819055506106728160040154866104ba565b6004820155600160a060020a03808816600090815260026020908152604080832033909416835292905220546106a890866104d4565b600160a060020a0380891660009081526002602090815260408083203385168085529252918290209390935590881691907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9088905190815260200160405180910390a36001935061071a565b600093505b5050509392505050565b6000805433600160a060020a0390811691161461074057600080fd5b60085460ff161561075057600080fd5b60075461076461075e6105ab565b846104ba565b111561076f57600080fd5b61077a836003610c7b565b905061078a8160040154836104ba565b600482015560055461079c90836104ba565b6005557f30385c845b448a36257a6a1716e6ad2e1bc2cbe333cde1e69fe849ad6511adfe8383604051600160a060020a03909216825260208201526040908101905180910390a15b505050565b60008060006107f6610d66565b151561080157600080fd5b61080c336002610c7b565b600381015490925090506000811161082357600080fd5b83151561083f57600f5461083790826104ba565b600f55610850565b600e5461084c90826104ba565b600e555b600060038301557f591a89b27b3057021df052ec15caa0a817c1894bcb52243ed0c8cdaa83f322be338583604051600160a060020a03909316835290151560208301526040808301919091526060909101905180910390a18092505b5050919050565b6000806108c1336001610c7b565b60028101549092509050801561015d5760006002830155600160a060020a03331681156108fc0282604051600060405180830381858888f19350505050151561015d57600080fd5b5b5b5050565b600160a060020a0381166000908152600160205260409020600401545b919050565b60005433600160a060020a0390811691161461094c57600080fd5b60085460ff161561095c57600080fd5b6008805460ff191660011790555b565b600c5460009081901561097e57600080fd5b60005433600160a060020a0390811691161461099957600080fd5b600654839010156109a957600080fd5b600083116109b657600080fd5b50600090508060c060405190810160409081528682526020820186905281018490524360608201526080810183905260a08101829052600a815181908051610a02929160200190610dc2565b506020820151600182015560408201518160020155606082015181600301556080820151816004015560a0820151600590910155505b5050505050565b60408051908101604052600381527f4d4f440000000000000000000000000000000000000000000000000000000000602082015281565b60085460009081908190819060ff161515610a9057600080fd5b610a9b336000610c7b565b925084836004015410158015610ab15750600085115b15610b4357610ac1336003610c7b565b9150610ace866003610c7b565b9050610ade8260040154866104d4565b8260040181905550610af48160040154866104ba565b6004820155600160a060020a038087169033167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8760405190815260200160405180910390a360019350610b48565b600093505b50505092915050565b600854600090819060ff161515610b6757600080fd5b60005433600160a060020a03908116911614610b8257600080fd5b600c5460009011610b9257600080fd5b610ba3600a600301546009546104ba565b4211610bae57600080fd5b600f54600e541115610c0f57600054610bd190600160a060020a03166003610c7b565b9150600a600201549050610be98260040154826104ba565b6004830155600554610bfb90826104ba565b600555600654610c0b90826104d4565b6006555b600a6000610c1d8282610e41565b506000600182018190556002820181905560038201819055600482018190556005909101555b5050565b600160a060020a038083166000908152600260209081526040808320938516835292905220545b92915050565b6005545b90565b600160a060020a03821660009081526001602052604081208160025b846003811115610ca357fe5b1480610cbb575060035b846003811115610cb957fe5b145b15610ceb57610cc8610d66565b8015610cd65750600d548254105b15610ceb5760048201546003830155600d5482555b5b60015b846003811115610cfb57fe5b1480610d13575060035b846003811115610d1157fe5b145b15610d5957610d2860035483600101546104d4565b90508015610d5957610d4b8260020154610d46838560040154610d93565b6104ba565b600283015560035460018301555b5b8192505b505092915050565b600c5460009015801590610d7c5750600d544310155b80156105bb5750600954600d540143105b90505b90565b6000828202831580610daf5750828482811515610dac57fe5b04145b15156104c957fe5b8091505b5092915050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610e0357805160ff1916838001178555610e30565b82800160010185558215610e30579182015b82811115610e30578251825591602001919060010190610e15565b5b50610e3d929150610e89565b5090565b50805460018160011615610100020316600290046000825580601f10610e675750610e85565b601f016020900490600052602060002090810190610e859190610e89565b5b50565b6105be91905b80821115610e3d5760008155600101610e8f565b5090565b905600a165627a7a723058201c7b4d0bde4531927f12aca7ab64e45b1b3edd65350d38d2a6283a85847564700029";

    private ModumToken(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private ModumToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<MintedEventResponse> getMintedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Minted", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<MintedEventResponse> responses = new ArrayList<MintedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            MintedEventResponse typedResponse = new MintedEventResponse();
            typedResponse._addr = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse.tokens = (Uint256) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<MintedEventResponse> mintedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Minted", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, MintedEventResponse>() {
            @Override
            public MintedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                MintedEventResponse typedResponse = new MintedEventResponse();
                typedResponse._addr = (Address) eventValues.getNonIndexedValues().get(0);
                typedResponse.tokens = (Uint256) eventValues.getNonIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public List<VotedEventResponse> getVotedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Voted", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<VotedEventResponse> responses = new ArrayList<VotedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            VotedEventResponse typedResponse = new VotedEventResponse();
            typedResponse._addr = (Address) eventValues.getNonIndexedValues().get(0);
            typedResponse.option = (Bool) eventValues.getNonIndexedValues().get(1);
            typedResponse.votes = (Uint256) eventValues.getNonIndexedValues().get(2);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<VotedEventResponse> votedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Voted", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, VotedEventResponse>() {
            @Override
            public VotedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                VotedEventResponse typedResponse = new VotedEventResponse();
                typedResponse._addr = (Address) eventValues.getNonIndexedValues().get(0);
                typedResponse.option = (Bool) eventValues.getNonIndexedValues().get(1);
                typedResponse.votes = (Uint256) eventValues.getNonIndexedValues().get(2);
                return typedResponse;
            }
        });
    }

    public List<PayoutEventResponse> getPayoutEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Payout", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<PayoutEventResponse> responses = new ArrayList<PayoutEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            PayoutEventResponse typedResponse = new PayoutEventResponse();
            typedResponse.weiPerToken = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<PayoutEventResponse> payoutEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Payout", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, PayoutEventResponse>() {
            @Override
            public PayoutEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                PayoutEventResponse typedResponse = new PayoutEventResponse();
                typedResponse.weiPerToken = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Transfer", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._to = (Address) eventValues.getIndexedValues().get(1);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Transfer", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._to = (Address) eventValues.getIndexedValues().get(1);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Approval", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse._owner = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._spender = (Address) eventValues.getIndexedValues().get(1);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ApprovalEventResponse> approvalEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Approval", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ApprovalEventResponse>() {
            @Override
            public ApprovalEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse._owner = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._spender = (Address) eventValues.getIndexedValues().get(1);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Future<Utf8String> name() {
        Function function = new Function("name", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> approve(Address _spender, Uint256 _value) {
        Function function = new Function("approve", Arrays.<Type>asList(_spender, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> totalSupply() {
        Function function = new Function("totalSupply", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> transferFrom(Address _from, Address _to, Uint256 _value) {
        Function function = new Function("transferFrom", Arrays.<Type>asList(_from, _to, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> mint(Address _recipient, Uint256 _value) {
        Function function = new Function("mint", Arrays.<Type>asList(_recipient, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> vote(Bool _vote) {
        Function function = new Function("vote", Arrays.<Type>asList(_vote), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> claimBonus() {
        Function function = new Function("claimBonus", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> balanceOf(Address _owner) {
        Function function = new Function("balanceOf", 
                Arrays.<Type>asList(_owner), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> mintFinished() {
        Function function = new Function("mintFinished", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> proposal(Utf8String _addr, Bytes32 _hash, Uint256 _value) {
        Function function = new Function("proposal", Arrays.<Type>asList(_addr, _hash, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Utf8String> symbol() {
        Function function = new Function("symbol", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> transfer(Address _to, Uint256 _value) {
        Function function = new Function("transfer", Arrays.<Type>asList(_to, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> claimProposal() {
        Function function = new Function("claimProposal", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> allowance(Address _owner, Address _spender) {
        Function function = new Function("allowance", 
                Arrays.<Type>asList(_owner, _spender), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> getUnlockedTokens() {
        Function function = new Function("getUnlockedTokens", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<ModumToken> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(ModumToken.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<ModumToken> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(ModumToken.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static ModumToken load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ModumToken(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ModumToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ModumToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class MintedEventResponse {
        public Address _addr;

        public Uint256 tokens;
    }

    public static class VotedEventResponse {
        public Address _addr;

        public Bool option;

        public Uint256 votes;
    }

    public static class PayoutEventResponse {
        public Uint256 weiPerToken;
    }

    public static class TransferEventResponse {
        public Address _from;

        public Address _to;

        public Uint256 _value;
    }

    public static class ApprovalEventResponse {
        public Address _owner;

        public Address _spender;

        public Uint256 _value;
    }
}
